package com.commerceos.paymentadapter.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.paymentadapter.domain.enums.PaymentGateway;
import com.commerceos.paymentadapter.domain.enums.TenderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payment_tenders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTender extends BaseEntity {

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "gateway", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentGateway gateway;

    @Column(name = "gateway_order_id")
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TenderStatus status = TenderStatus.PENDING;

    @Column(name = "failure_reason")
    private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_response", columnDefinition = "jsonb")
    private String rawResponse;
}
