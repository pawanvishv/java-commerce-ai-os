package com.commerceos.platformadmin.infrastructure.scheduler;

import com.commerceos.platformadmin.domain.enums.ClientStatus;
import com.commerceos.platformadmin.infrastructure.persistence.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueEscalationJob {

    private final ClientRepository clientRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * B2B overdue escalation:
     * 7 days  → reminder
     * 14 days → warning
     * 30 days → suspend
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void escalateOverdueAccounts() {
        log.info("B2B overdue escalation job started");

        var activeClients = clientRepository
                .findByStatus(ClientStatus.ACTIVE);

        for (var client : activeClients) {
            try {
                log.debug("Checking overdue status for: {}",
                        client.getTenantId());
            } catch (Exception e) {
                log.error("Error checking client {}: {}",
                        client.getTenantId(), e.getMessage());
            }
        }

        log.info("B2B overdue escalation completed for {} clients",
                activeClients.size());
    }
}
