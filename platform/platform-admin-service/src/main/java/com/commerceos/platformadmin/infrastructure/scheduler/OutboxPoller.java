package com.commerceos.platformadmin.infrastructure.scheduler;

import com.commerceos.platformadmin.domain.enums.OutboxStatus;
import com.commerceos.platformadmin.domain.model.OutboxEvent;
import com.commerceos.platformadmin.infrastructure.persistence.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void pollAndPublish() {

        try {
            List<OutboxEvent> events = outboxEventRepository.findPendingEventsForUpdate(BATCH_SIZE);

            if (events.isEmpty()) {
                log.debug("No pending outbox events found.");
                return;
            }

            log.debug("Processing {} outbox events", events.size());

            for (OutboxEvent event : events) {
                try {
                    // Blocking send to catch exceptions
                    kafkaTemplate.send(
                            event.getEventType(),
                            event.getAggregateId(),
                            event.getPayload()
                    ).get();

                    // Mark event as processed
                    event.setStatus(OutboxStatus.PROCESSED);
                    event.setProcessedAt(Instant.now());
                    log.debug("Published outbox event: {} for {}", event.getEventType(), event.getAggregateId());

                } catch (ExecutionException | InterruptedException e) {
                    log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage(), e);

                    // Retry logic
                    int retryCount = event.getRetryCount() + 1;
                    event.setRetryCount(retryCount);
                    event.setErrorMessage(e.getMessage());

                    if (retryCount >= MAX_RETRIES) {
                        event.setStatus(OutboxStatus.DEAD);
                        log.error("Outbox event {} moved to DLQ after {} retries", event.getId(), MAX_RETRIES);
                    }
                } catch (Exception e) {
                    log.error("Unexpected error for event {}: {}", event.getId(), e.getMessage(), e);
                    event.setErrorMessage(e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("CRITICAL: Outbox poller crashed", e);
        }
    }
}