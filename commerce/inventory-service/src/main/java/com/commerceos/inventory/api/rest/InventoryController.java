package com.commerceos.inventory.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.inventory.application.handlers.InventoryService;
import com.commerceos.inventory.domain.model.PendingReservation;
import com.commerceos.inventory.domain.model.StoreInventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/stores/{storeId}/skus/{sku}/stock")
    public ResponseEntity<ApiResponse<StoreInventory>> addStock(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String storeId,
            @PathVariable String sku,
            @RequestParam long qty) {

        StoreInventory inventory = inventoryService
                .addStock(tenantId, storeId, sku, qty);
        return ResponseEntity.ok(ApiResponse.ok(inventory,
                "Stock added successfully"));
    }

    @GetMapping("/stores/{storeId}/skus/{sku}")
    public ResponseEntity<ApiResponse<StoreInventory>> getInventory(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String storeId,
            @PathVariable String sku) {

        StoreInventory inventory = inventoryService
                .getInventory(tenantId, storeId, sku);
        return ResponseEntity.ok(ApiResponse.ok(inventory));
    }

    @PostMapping("/stores/{storeId}/skus/{sku}/reserve")
    public ResponseEntity<ApiResponse<PendingReservation>> reserve(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String storeId,
            @PathVariable String sku,
            @RequestParam String orderId,
            @RequestParam long qty) {

        PendingReservation reservation = inventoryService
                .reserve(tenantId, storeId, sku, orderId, qty);
        return ResponseEntity.ok(ApiResponse.ok(reservation,
                "Inventory reserved"));
    }

    @PostMapping("/stores/{storeId}/skus/{sku}/confirm")
    public ResponseEntity<ApiResponse<String>> confirm(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String storeId,
            @PathVariable String sku,
            @RequestParam String orderId) {

        inventoryService.confirmReservation(
                tenantId, storeId, sku, orderId);
        return ResponseEntity.ok(
                ApiResponse.ok("Reservation confirmed"));
    }
}
