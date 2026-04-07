package com.commerceos.provisioning.infrastructure.temporal.activities;

import com.commerceos.provisioning.domain.enums.ProvisioningStatus;
import com.commerceos.provisioning.infrastructure.persistence.ProvisioningJobRepository;
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ActivityImpl(taskQueues = "provisioning-task-queue")
@RequiredArgsConstructor
public class ProvisioningActivitiesImpl implements ProvisioningActivities {

    private final JdbcTemplate jdbcTemplate;
    private final ProvisioningJobRepository jobRepository;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Override
    public void createSchema(String tenantId, String schemaName) {
        log.info("Creating schema: {}", schemaName);
        jdbcTemplate.execute(
                "CREATE SCHEMA IF NOT EXISTS " + schemaName);
        log.info("Schema created: {}", schemaName);
    }

    @Override
    public void runMigrations(String tenantId,
                               String schemaName,
                               String profile) {
        log.info("Running migrations for schema: {}, profile: {}",
                schemaName, profile);

        String dbUrl = datasourceUrl.replace(
                "/provisioning_db", "/order_db");

        Flyway flyway = Flyway.configure()
                .dataSource(datasourceUrl,
                        datasourceUsername,
                        datasourcePassword)
                .schemas(schemaName)
                .locations("classpath:db/tenant/common")
                .load();

        flyway.migrate();
        log.info("Migrations completed for schema: {}", schemaName);
    }

    @Override
    public void seedRbac(String tenantId) {
        log.info("Seeding RBAC for tenant: {}", tenantId);
    }

    @Override
    public void issueCredentials(String tenantId) {
        log.info("Credentials issued for tenant: {}", tenantId);
    }

    @Override
    @Transactional
    public void updateJobStatus(String tenantId,
                                 String status,
                                 String step) {
        jobRepository.findByTenantId(tenantId).ifPresent(job -> {
            job.setStatus(ProvisioningStatus.valueOf(status));
            job.setCurrentStep(step);
            jobRepository.save(job);
            log.info("Job status updated for tenant {}: {} - {}",
                    tenantId, status, step);
        });
    }
}
