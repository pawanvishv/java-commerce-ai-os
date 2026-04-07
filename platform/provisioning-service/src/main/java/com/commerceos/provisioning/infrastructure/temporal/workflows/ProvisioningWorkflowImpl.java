package com.commerceos.provisioning.infrastructure.temporal.workflows;

import com.commerceos.provisioning.infrastructure.temporal.activities.ProvisioningActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class ProvisioningWorkflowImpl implements ProvisioningWorkflow {

    private final ProvisioningActivities activities =
            Workflow.newActivityStub(ProvisioningActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofMinutes(5))
                            .setRetryOptions(RetryOptions.newBuilder()
                                    .setMaximumAttempts(3)
                                    .build())
                            .build());

    @Override
    public void provision(String tenantId, String profile) {
        String schemaName = "tenant_" + tenantId.replace("-", "_");

        activities.updateJobStatus(tenantId, "IN_PROGRESS",
                "CREATE_SCHEMA");
        activities.createSchema(tenantId, schemaName);

        activities.updateJobStatus(tenantId, "IN_PROGRESS",
                "RUN_MIGRATIONS");
        activities.runMigrations(tenantId, schemaName, profile);

        activities.updateJobStatus(tenantId, "IN_PROGRESS",
                "SEED_RBAC");
        activities.seedRbac(tenantId);

        activities.updateJobStatus(tenantId, "IN_PROGRESS",
                "ISSUE_CREDENTIALS");
        activities.issueCredentials(tenantId);

        activities.updateJobStatus(tenantId, "COMPLETED", null);
    }
}
