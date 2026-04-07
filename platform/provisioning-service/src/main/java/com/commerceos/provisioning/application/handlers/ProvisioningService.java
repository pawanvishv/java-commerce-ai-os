package com.commerceos.provisioning.application.handlers;

import com.commerceos.provisioning.domain.enums.ProvisioningStatus;
import com.commerceos.provisioning.domain.model.ProvisioningJob;
import com.commerceos.provisioning.infrastructure.persistence.ProvisioningJobRepository;
import com.commerceos.provisioning.infrastructure.temporal.workflows.ProvisioningWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProvisioningService {

    private final ProvisioningJobRepository jobRepository;

    @Autowired(required = false)
    private WorkflowClient workflowClient;

    private static final String TASK_QUEUE = "provisioning-task-queue";

    public ProvisioningService(ProvisioningJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public ProvisioningJob startProvisioning(String tenantId, String profile) {
        if (jobRepository.findByTenantId(tenantId).isPresent()) {
            throw new IllegalStateException(
                    "Provisioning already exists for tenant: " + tenantId);
        }

        String workflowId = "provision-" + tenantId;

        ProvisioningJob job = ProvisioningJob.builder()
                .tenantId(tenantId)
                .workflowId(workflowId)
                .profile(profile)
                .status(ProvisioningStatus.PENDING)
                .build();

        job = jobRepository.save(job);

        if (workflowClient != null) {
            try {
                ProvisioningWorkflow workflow = workflowClient.newWorkflowStub(
                        ProvisioningWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(TASK_QUEUE)
                                .build());
                WorkflowClient.start(workflow::provision, tenantId, profile);
                log.info("Started Temporal workflow for tenant: {}", tenantId);
            } catch (Exception e) {
                log.warn("Could not start Temporal workflow: {}", e.getMessage());
                job.setStatus(ProvisioningStatus.PENDING);
                jobRepository.save(job);
            }
        } else {
            log.warn("Temporal not available — job saved as PENDING: {}",
                    tenantId);
        }

        return job;
    }

    public ProvisioningJob getJob(String tenantId) {
        return jobRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No provisioning job for tenant: " + tenantId));
    }
}
