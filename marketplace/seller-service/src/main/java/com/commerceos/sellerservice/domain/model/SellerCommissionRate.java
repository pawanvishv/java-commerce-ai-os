package com.commerceos.sellerservice.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seller_commission_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerCommissionRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "category")
    private String category;

    @Column(name = "rate_percent", nullable = false)
    private BigDecimal ratePercent;

    @Column(name = "effective_from")
    @Builder.Default
    private Instant effectiveFrom = Instant.now();

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
