package com.commerceos.taxcalculation.application.commands;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationRequest {

    @NotNull
    @Min(0)
    private Long basePricePaise;

    @NotBlank
    private String taxMode;

    @NotBlank
    private String taxCodeType;

    @NotBlank
    private String taxCode;

    @NotNull
    @Min(1)
    private Integer qty;

    private String uom;
}
