package com.commerceos.provisioning.infrastructure.temporal.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ProvisioningActivities {

    @ActivityMethod
    void createSchema(String tenantId, String schemaName);

    @ActivityMethod
    void runMigrations(String tenantId, String schemaName, String profile);

    @ActivityMethod
    void seedRbac(String tenantId);

    @ActivityMethod
    void issueCredentials(String tenantId);

    @ActivityMethod
    void updateJobStatus(String tenantId, String status, String step);
}
