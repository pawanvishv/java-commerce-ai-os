package com.commerceos.ledger.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.ledger.application.handlers.LedgerService;
import com.commerceos.ledger.domain.model.LedgerEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping("/orders/{orderId}/post")
    public ResponseEntity<ApiResponse<String>> postPayment(
            @PathVariable String orderId,
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam long totalPaise,
            @RequestParam long taxPaise) {

        ledgerService.postOrderPayment(
                tenantId, orderId, totalPaise, taxPaise, orderId);
        return ResponseEntity.ok(
                ApiResponse.ok("Ledger entries posted"));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<List<LedgerEntry>>> getEntries(
            @PathVariable String orderId,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        List<LedgerEntry> entries = ledgerService
                .getEntriesForOrder(tenantId, orderId);
        return ResponseEntity.ok(ApiResponse.ok(entries));
    }
}
