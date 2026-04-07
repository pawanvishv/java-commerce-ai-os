package com.commerceos.sellerservice.application.handlers;

import com.commerceos.sellerservice.domain.enums.PayoutStatus;
import com.commerceos.sellerservice.domain.enums.SellerStatus;
import com.commerceos.sellerservice.domain.model.Seller;
import com.commerceos.sellerservice.domain.model.SellerCommissionRate;
import com.commerceos.sellerservice.domain.model.SellerPayout;
import com.commerceos.sellerservice.infrastructure.persistence.CommissionRateRepository;
import com.commerceos.sellerservice.infrastructure.persistence.PayoutRepository;
import com.commerceos.sellerservice.infrastructure.persistence.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final CommissionRateRepository commissionRateRepository;
    private final PayoutRepository payoutRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final BigDecimal TCS_RATE =
            new BigDecimal("0.001");
    private static final BigDecimal DEFAULT_COMMISSION =
            new BigDecimal("10.00");

    @Transactional
    public Seller registerSeller(String tenantId,
                                  String businessName,
                                  String email,
                                  String phone) {
        String sellerId = "SLR-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();

        Seller seller = Seller.builder()
                .tenantId(tenantId)
                .sellerId(sellerId)
                .businessName(businessName)
                .email(email)
                .phone(phone)
                .status(SellerStatus.PENDING_KYC)
                .build();

        seller = sellerRepository.save(seller);
        log.info("Seller registered: {} for tenant: {}",
                sellerId, tenantId);
        return seller;
    }

    @Transactional
    public Seller activateSeller(String sellerId,
                                  String tenantId,
                                  BigDecimal commissionRate) {
        Seller seller = sellerRepository
                .findBySellerIdAndTenantId(sellerId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seller not found: " + sellerId));

        seller.setStatus(SellerStatus.ACTIVE);
        seller = sellerRepository.save(seller);

        SellerCommissionRate rate = SellerCommissionRate.builder()
                .sellerId(sellerId)
                .tenantId(tenantId)
                .ratePercent(commissionRate != null
                        ? commissionRate : DEFAULT_COMMISSION)
                .build();
        commissionRateRepository.save(rate);

        log.info("Seller {} activated with commission rate: {}%",
                sellerId, rate.getRatePercent());
        return seller;
    }

    /**
     * Process seller sale — consume seller.sale.v1
     * Calculate commission and TCS
     * Create payout
     */
    @Transactional
    public void processSale(String sellerId, String tenantId,
                             String orderId, long salePaise) {
        SellerCommissionRate rate = commissionRateRepository
                .findLatestRate(sellerId, tenantId)
                .orElse(SellerCommissionRate.builder()
                        .ratePercent(DEFAULT_COMMISSION)
                        .build());

        long commissionPaise = BigDecimal.valueOf(salePaise)
                .multiply(rate.getRatePercent())
                .divide(new BigDecimal("100"))
                .longValue();

        long tcsPaise = BigDecimal.valueOf(salePaise)
                .multiply(TCS_RATE)
                .longValue();

        long netPayoutPaise = salePaise
                - commissionPaise - tcsPaise;

        SellerPayout payout = SellerPayout.builder()
                .sellerId(sellerId)
                .tenantId(tenantId)
                .amountPaise(netPayoutPaise)
                .status(PayoutStatus.PENDING)
                .referenceId(orderId)
                .build();

        payoutRepository.save(payout);

        publishEvent("seller.payout.created.v1", tenantId,
                Map.of(
                        "sellerId", sellerId,
                        "orderId", orderId,
                        "salePaise", salePaise,
                        "commissionPaise", commissionPaise,
                        "tcsPaise", tcsPaise,
                        "netPayoutPaise", netPayoutPaise
                ));

        log.info("Sale processed for seller {}: " +
                        "sale={} commission={} tcs={} net={}",
                sellerId, salePaise, commissionPaise,
                tcsPaise, netPayoutPaise);
    }

    public Seller getSeller(String sellerId, String tenantId) {
        return sellerRepository
                .findBySellerIdAndTenantId(sellerId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seller not found: " + sellerId));
    }

    public List<Seller> getActiveSellers(String tenantId) {
        return sellerRepository.findByTenantIdAndStatus(
                tenantId, SellerStatus.ACTIVE);
    }

    public List<SellerPayout> getPayouts(String sellerId,
                                          String tenantId) {
        return payoutRepository
                .findBySellerIdAndTenantId(sellerId, tenantId);
    }

    private void publishEvent(String topic, String key,
                               Object payload) {
        try {
            kafkaTemplate.send(topic, key, payload);
        } catch (Exception e) {
            log.warn("Failed to publish {}: {}",
                    topic, e.getMessage());
        }
    }
}
