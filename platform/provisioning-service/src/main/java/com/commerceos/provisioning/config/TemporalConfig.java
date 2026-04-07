package com.commerceos.provisioning.config;

import com.commerceos.provisioning.infrastructure.temporal.activities.ProvisioningActivitiesImpl;
import com.commerceos.provisioning.infrastructure.temporal.workflows.ProvisioningWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TemporalConfig {

    @Value("${temporal.connection.target:localhost:7233}")
    private String temporalTarget;

    @Value("${temporal.namespace:default}")
    private String namespace;

    @Value("${temporal.worker.task-queue:provisioning-task-queue}")
    private String taskQueue;

    private final ProvisioningActivitiesImpl provisioningActivities;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalTarget)
                        .setRpcTimeout(Duration.ofSeconds(10))
                        .setConnectionBackoffResetFrequency(Duration.ofSeconds(10))
                        .setGrpcReconnectFrequency(Duration.ofSeconds(10))
                        .build());
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs stubs) {
        return WorkflowClient.newInstance(stubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(namespace)
                        .build());
    }

    @Bean
    @Lazy
    public WorkerFactory workerFactory(WorkflowClient client) {
        try {
            WorkerFactory factory = WorkerFactory.newInstance(client);
            Worker worker = factory.newWorker(taskQueue);
            worker.registerWorkflowImplementationTypes(
                    ProvisioningWorkflowImpl.class);
            worker.registerActivitiesImplementations(
                    provisioningActivities);
            factory.start();
            log.info("Temporal worker started on task queue: {}", taskQueue);
            return factory;
        } catch (Exception e) {
            log.warn("Temporal not available — worker not started: {}",
                    e.getMessage());
            return null;
        }
    }
}
