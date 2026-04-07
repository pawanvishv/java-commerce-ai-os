package com.commerceos.catalog.infrastructure.persistence;

import com.commerceos.catalog.domain.enums.ItemStatus;
import com.commerceos.catalog.domain.enums.ItemType;
import com.commerceos.catalog.domain.model.CatalogItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogItemRepository
        extends MongoRepository<CatalogItem, String> {

    Optional<CatalogItem> findByTenantIdAndSku(
            String tenantId, String sku);

    List<CatalogItem> findByTenantIdAndStatus(
            String tenantId, ItemStatus status);

    List<CatalogItem> findByTenantIdAndItemTypeAndStatus(
            String tenantId, ItemType itemType, ItemStatus status);

    boolean existsByTenantIdAndSku(String tenantId, String sku);
}
