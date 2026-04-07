package com.commerceos.orderservice.domain.model;

import com.commerceos.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "saga_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaState extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "workflow_id")
    private String workflowId;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "STARTED";

    @Column(name = "current_step")
    private String currentStep;

    @Column(name = "error_message")
    private String errorMessage;
}
