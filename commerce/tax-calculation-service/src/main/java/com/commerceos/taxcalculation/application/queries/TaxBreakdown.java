package com.commerceos.taxcalculation.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxBreakdown {

    private String taxType;
    private BigDecimal rate;
    private long amountPaise;
}
