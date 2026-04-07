package com.commerceos.provisioning.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.provisioning.domain.enums.ProvisioningStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provisioning_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvisioningJob extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, unique = true)
    private String tenantId;

    @Column(name = "workflow_id")
    private String workflowId;

    @Column(name = "profile", nullable = false)
    private String profile;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProvisioningStatus status = ProvisioningStatus.PENDING;

    @Column(name = "current_step")
    private String currentStep;

    @Column(name = "error_message")
    private String errorMessage;
}
