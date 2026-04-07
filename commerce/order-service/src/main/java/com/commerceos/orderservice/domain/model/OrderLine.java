package com.commerceos.orderservice.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.orderservice.domain.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Column(name = "qty", nullable = false)
    private int qty;

    @Column(name = "unit_price_paise", nullable = false)
    private long unitPricePaise;

    @Column(name = "tax_paise", nullable = false)
    @Builder.Default
    private long taxPaise = 0;

    @Column(name = "total_paise", nullable = false)
    private long totalPaise;
}
