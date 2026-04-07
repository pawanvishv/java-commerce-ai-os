package com.commerceos.inventory.infrastructure.persistence;

import com.commerceos.inventory.domain.model.StoreInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreInventoryRepository
        extends JpaRepository<StoreInventory, UUID> {

    Optional<StoreInventory> findByTenantIdAndStoreIdAndSku(
            String tenantId, String storeId, String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i FROM StoreInventory i
        WHERE i.tenantId = :tenantId
        AND i.storeId = :storeId
        AND i.sku = :sku
        """)
    Optional<StoreInventory> findByTenantIdAndStoreIdAndSkuForUpdate(
            @Param("tenantId") String tenantId,
            @Param("storeId") String storeId,
            @Param("sku") String sku);
}
