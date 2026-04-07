package com.commerceos.taxcalculation.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.taxcalculation.application.commands.TaxCalculationRequest;
import com.commerceos.taxcalculation.application.handlers.TaxCalculationService;
import com.commerceos.taxcalculation.application.queries.TaxCalculationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxCalculationService taxService;

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<TaxCalculationResponse>> calculate(
            @Valid @RequestBody TaxCalculationRequest request) {
        log.info("Tax calculation for code: {} type: {}",
                request.getTaxCode(), request.getTaxCodeType());
        TaxCalculationResponse response = taxService.calculate(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
