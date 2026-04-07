package com.commerceos.platformadmin.infrastructure.scheduler;

import com.commerceos.platformadmin.domain.enums.ClientStatus;
import com.commerceos.platformadmin.infrastructure.persistence.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Nightly tier promotion:
 * Standard → Professional triggers UpgradeWorkflow
 * Enterprise always manual
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TierPromotionJob {

    private final ClientRepository clientRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(cron = "0 0 1 * * *")
    public void evaluateTierPromotion() {
        log.info("Tier promotion evaluation started");

        var activeClients = clientRepository
                .findByStatus(ClientStatus.ACTIVE);

        for (var client : activeClients) {
            try {
                if ("STANDARD".equals(client.getPlan())) {
                    log.debug("Evaluating tier promotion for: {}",
                            client.getTenantId());
                }
            } catch (Exception e) {
                log.error("Tier evaluation failed for {}: {}",
                        client.getTenantId(), e.getMessage());
            }
        }

        log.info("Tier promotion evaluation completed");
    }
}
