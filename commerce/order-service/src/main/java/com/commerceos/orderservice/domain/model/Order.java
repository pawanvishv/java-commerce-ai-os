package com.commerceos.orderservice.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.orderservice.domain.enums.OrderChannel;
import com.commerceos.orderservice.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_paise", nullable = false)
    @Builder.Default
    private long totalPaise = 0;

    @Column(name = "tax_paise", nullable = false)
    @Builder.Default
    private long taxPaise = 0;

    @Column(name = "channel", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderChannel channel = OrderChannel.ONLINE;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrderLine> lines = new ArrayList<>();
}
