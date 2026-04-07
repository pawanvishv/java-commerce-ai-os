package com.commerceos.provisioning.infrastructure.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ProvisioningWorkflow {

    @WorkflowMethod
    void provision(String tenantId, String profile);
}
