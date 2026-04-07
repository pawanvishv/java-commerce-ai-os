package com.commerceos.inventory.domain.model;

import com.commerceos.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInventory extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "available_qty", nullable = false)
    @Builder.Default
    private long availableQty = 0;

    @Column(name = "reserved_qty", nullable = false)
    @Builder.Default
    private long reservedQty = 0;

    @Column(name = "damaged_qty", nullable = false)
    @Builder.Default
    private long damagedQty = 0;

    @Column(name = "expired_qty", nullable = false)
    @Builder.Default
    private long expiredQty = 0;
}
