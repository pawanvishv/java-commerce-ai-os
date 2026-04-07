package com.commerceos.taxcalculation.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationResponse {

    private long basePricePaise;
    private long totalTaxPaise;
    private long totalPaise;
    private String displayMode;
    private List<TaxBreakdown> taxBreakdown;
}
