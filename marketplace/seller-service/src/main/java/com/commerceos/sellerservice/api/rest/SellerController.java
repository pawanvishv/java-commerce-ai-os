package com.commerceos.sellerservice.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.sellerservice.application.handlers.SellerService;
import com.commerceos.sellerservice.domain.model.Seller;
import com.commerceos.sellerservice.domain.model.SellerPayout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<ApiResponse<Seller>> register(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String businessName,
            @RequestParam String email,
            @RequestParam(required = false) String phone) {

        Seller seller = sellerService.registerSeller(
                tenantId, businessName, email, phone);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(seller,
                        "Seller registered"));
    }

    @PostMapping("/{sellerId}/activate")
    public ResponseEntity<ApiResponse<Seller>> activate(
            @PathVariable String sellerId,
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam(required = false) BigDecimal commissionRate) {

        Seller seller = sellerService.activateSeller(
                sellerId, tenantId, commissionRate);
        return ResponseEntity.ok(ApiResponse.ok(seller,
                "Seller activated"));
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<Seller>> getSeller(
            @PathVariable String sellerId,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        Seller seller = sellerService.getSeller(
                sellerId, tenantId);
        return ResponseEntity.ok(ApiResponse.ok(seller));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Seller>>> listActive(
            @RequestHeader("X-Tenant-ID") String tenantId) {

        List<Seller> sellers = sellerService
                .getActiveSellers(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(sellers));
    }

    @GetMapping("/{sellerId}/payouts")
    public ResponseEntity<ApiResponse<List<SellerPayout>>> payouts(
            @PathVariable String sellerId,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        List<SellerPayout> payouts = sellerService
                .getPayouts(sellerId, tenantId);
        return ResponseEntity.ok(ApiResponse.ok(payouts));
    }

    @PostMapping("/{sellerId}/sales/simulate")
    public ResponseEntity<ApiResponse<String>> simulateSale(
            @PathVariable String sellerId,
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String orderId,
            @RequestParam long salePaise) {

        sellerService.processSale(
                sellerId, tenantId, orderId, salePaise);
        return ResponseEntity.ok(ApiResponse.ok(
                "Sale processed for seller: " + sellerId));
    }
}
