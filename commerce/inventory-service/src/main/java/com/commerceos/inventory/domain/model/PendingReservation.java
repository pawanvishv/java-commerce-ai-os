package com.commerceos.inventory.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.inventory.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "pending_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingReservation extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "qty", nullable = false)
    private long qty;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
