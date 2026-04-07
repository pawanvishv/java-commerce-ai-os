package com.commerceos.sellerservice.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.sellerservice.domain.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPayout extends BaseEntity {

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;
}
