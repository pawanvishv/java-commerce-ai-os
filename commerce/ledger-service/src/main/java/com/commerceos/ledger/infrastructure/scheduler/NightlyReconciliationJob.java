package com.commerceos.ledger.infrastructure.scheduler;

import com.commerceos.ledger.infrastructure.persistence.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NightlyReconciliationJob {

    private final LedgerEntryRepository ledgerEntryRepository;

    /**
     * Tier 3 — nightly batch reconciliation
     * Runs at 2 AM every day
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void reconcileTier3() {
        log.info("Tier 3 nightly reconciliation started");
        try {
            long totalEntries = ledgerEntryRepository.count();
            log.info("Tier 3: total ledger entries: {}",
                    totalEntries);
            log.info("Tier 3 reconciliation completed");
        } catch (Exception e) {
            log.error("Tier 3 reconciliation failed: {}",
                    e.getMessage(), e);
        }
    }
}
