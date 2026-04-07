package com.commerceos.catalog.application.handlers;

import com.commerceos.catalog.domain.enums.ItemStatus;
import com.commerceos.catalog.domain.model.CatalogItem;
import com.commerceos.catalog.infrastructure.persistence.CatalogItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogItemRepository catalogItemRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CatalogItem createItem(CatalogItem item) {
        if (catalogItemRepository.existsByTenantIdAndSku(
                item.getTenantId(), item.getSku())) {
            throw new IllegalArgumentException(
                    "Item already exists with SKU: " + item.getSku());
        }

        item.setStatus(ItemStatus.ACTIVE);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        item = catalogItemRepository.save(item);

        publishItemUpdated(item);
        log.info("Created catalog item: {} for tenant: {}",
                item.getSku(), item.getTenantId());
        return item;
    }

    public CatalogItem updateItem(String id, CatalogItem updates) {
        CatalogItem existing = catalogItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found: " + id));

        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setBasePricePaise(updates.getBasePricePaise());
        existing.setTags(updates.getTags());
        existing.setAttributes(updates.getAttributes());
        existing.setUpdatedAt(Instant.now());

        existing = catalogItemRepository.save(existing);
        publishItemUpdated(existing);
        return existing;
    }

    public List<CatalogItem> getActiveItems(String tenantId) {
        return catalogItemRepository
                .findByTenantIdAndStatus(tenantId, ItemStatus.ACTIVE);
    }

    public CatalogItem getItemBySku(String tenantId, String sku) {
        return catalogItemRepository
                .findByTenantIdAndSku(tenantId, sku)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found: " + sku));
    }

    private void publishItemUpdated(CatalogItem item) {
        try {
            kafkaTemplate.send("catalog.item.updated.v1",
                    item.getTenantId(),
                    Map.of(
                            "itemId", item.getId(),
                            "tenantId", item.getTenantId(),
                            "sku", item.getSku(),
                            "status", item.getStatus().name()
                    ));
        } catch (Exception e) {
            log.warn("Failed to publish catalog event: {}",
                    e.getMessage());
        }
    }
}
