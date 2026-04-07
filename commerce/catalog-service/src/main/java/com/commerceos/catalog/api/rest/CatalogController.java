package com.commerceos.catalog.api.rest;

import com.commerceos.catalog.application.handlers.CatalogService;
import com.commerceos.catalog.domain.model.CatalogItem;
import com.commerceos.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CatalogItem>> createItem(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody CatalogItem item) {
        item.setTenantId(tenantId);
        CatalogItem created = catalogService.createItem(item);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Item created"));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<CatalogItem>>> getItems(
            @RequestHeader("X-Tenant-ID") String tenantId) {
        List<CatalogItem> items = catalogService
                .getActiveItems(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @GetMapping("/items/sku/{sku}")
    public ResponseEntity<ApiResponse<CatalogItem>> getItemBySku(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String sku) {
        CatalogItem item = catalogService.getItemBySku(tenantId, sku);
        return ResponseEntity.ok(ApiResponse.ok(item));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CatalogItem>> updateItem(
            @PathVariable String id,
            @RequestBody CatalogItem updates) {
        CatalogItem updated = catalogService.updateItem(id, updates);
        return ResponseEntity.ok(ApiResponse.ok(updated,
                "Item updated"));
    }
}
