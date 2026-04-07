package com.commerceos.taxcalculation.application.handlers;

import com.commerceos.common.util.MoneyUtils;
import com.commerceos.taxcalculation.application.commands.TaxCalculationRequest;
import com.commerceos.taxcalculation.application.queries.TaxBreakdown;
import com.commerceos.taxcalculation.application.queries.TaxCalculationResponse;
import com.commerceos.taxcalculation.domain.model.HsnSacSlab;
import com.commerceos.taxcalculation.infrastructure.persistence.HsnSacSlabRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    private final HsnSacSlabRepository slabRepository;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public TaxCalculationResponse calculate(TaxCalculationRequest req) {
        HsnSacSlab slab = slabRepository
                .findActiveSlab(req.getTaxCode(), req.getTaxCodeType())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No tax slab found for code: " + req.getTaxCode()));

        long lineTotal = req.getBasePricePaise() * req.getQty();
        boolean inclusive = "TAX_INCLUSIVE".equals(req.getTaxMode());

        long basePaise;
        if (inclusive) {
            BigDecimal totalRate = slab.getCgstRate()
                    .add(slab.getSgstRate())
                    .add(slab.getCessRate());
            BigDecimal divisor = BigDecimal.ONE.add(
                    totalRate.divide(HUNDRED, 10, RoundingMode.HALF_UP));
            basePaise = BigDecimal.valueOf(lineTotal)
                    .divide(divisor, 0, RoundingMode.HALF_UP)
                    .longValueExact();
        } else {
            basePaise = lineTotal;
        }

        List<TaxBreakdown> breakdown = new ArrayList<>();
        long totalTax = 0;

        if (slab.getCgstRate().compareTo(BigDecimal.ZERO) > 0) {
            long cgst = MoneyUtils.percentOf(basePaise, slab.getCgstRate());
            breakdown.add(TaxBreakdown.builder()
                    .taxType("CGST")
                    .rate(slab.getCgstRate())
                    .amountPaise(cgst)
                    .build());
            totalTax += cgst;
        }

        if (slab.getSgstRate().compareTo(BigDecimal.ZERO) > 0) {
            long sgst = MoneyUtils.percentOf(basePaise, slab.getSgstRate());
            breakdown.add(TaxBreakdown.builder()
                    .taxType("SGST")
                    .rate(slab.getSgstRate())
                    .amountPaise(sgst)
                    .build());
            totalTax += sgst;
        }

        if (slab.getCessRate().compareTo(BigDecimal.ZERO) > 0) {
            long cess = MoneyUtils.percentOf(basePaise, slab.getCessRate());
            breakdown.add(TaxBreakdown.builder()
                    .taxType("CESS")
                    .rate(slab.getCessRate())
                    .amountPaise(cess)
                    .build());
            totalTax += cess;
        }

        long totalPaise = inclusive
                ? lineTotal
                : basePaise + totalTax;

        log.debug("Tax calculated — base: {}, tax: {}, total: {}",
                basePaise, totalTax, totalPaise);

        return TaxCalculationResponse.builder()
                .basePricePaise(basePaise)
                .totalTaxPaise(totalTax)
                .totalPaise(totalPaise)
                .displayMode(req.getTaxMode())
                .taxBreakdown(breakdown)
                .build();
    }
}
