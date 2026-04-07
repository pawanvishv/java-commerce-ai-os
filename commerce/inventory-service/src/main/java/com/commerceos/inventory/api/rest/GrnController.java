package com.commerceos.inventory.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.inventory.application.handlers.GrnService;
import com.commerceos.inventory.domain.model.StoreInventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory/grn")
@RequiredArgsConstructor
public class GrnController {

    private final GrnService grnService;

    @PostMapping("/receive")
    public ResponseEntity<ApiResponse<StoreInventory>> receive(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String storeId,
            @RequestParam String sku,
            @RequestParam String poNumber,
            @RequestParam long receivedQty,
            @RequestParam long expectedQty) {

        StoreInventory inventory = grnService.receiveGoods(
                tenantId, storeId, sku,
                poNumber, receivedQty, expectedQty);
        return ResponseEntity.ok(ApiResponse.ok(inventory,
                "Goods received successfully"));
    }

    @PostMapping("/damaged")
    public ResponseEntity<ApiResponse<StoreInventory>> receiveDamaged(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String storeId,
            @RequestParam String sku,
            @RequestParam long damagedQty) {

        StoreInventory inventory = grnService.receiveDamagedGoods(
                tenantId, storeId, sku, damagedQty);
        return ResponseEntity.ok(ApiResponse.ok(inventory,
                "Damaged goods recorded"));
    }
}
