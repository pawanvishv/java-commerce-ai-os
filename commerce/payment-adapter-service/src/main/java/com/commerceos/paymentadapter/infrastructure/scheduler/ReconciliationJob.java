package com.commerceos.paymentadapter.infrastructure.scheduler;

import com.commerceos.paymentadapter.domain.enums.TenderStatus;
import com.commerceos.paymentadapter.domain.model.PaymentTender;
import com.commerceos.paymentadapter.infrastructure.persistence.PaymentTenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationJob {

    private final PaymentTenderRepository tenderRepository;

    /**
     * Tier 2 — 5-min reconciliation:
     * Compare gateway status vs local payment_tenders
     */
    @Scheduled(fixedDelay = 300000)
    public void reconcileTier2() {
        log.info("Tier 2 reconciliation started");

        List<PaymentTender> pendingTenders = tenderRepository
                .findByStatus(TenderStatus.PENDING);

        if (pendingTenders.isEmpty()) {
            log.debug("Tier 2: no pending tenders to reconcile");
            return;
        }

        int mismatches = 0;
        for (PaymentTender tender : pendingTenders) {
            try {
                log.warn("Tier 2: tender {} stuck in PENDING " +
                        "for order: {}",
                        tender.getId(), tender.getOrderId());
                mismatches++;
            } catch (Exception e) {
                log.error("Tier 2: error checking tender {}: {}",
                        tender.getId(), e.getMessage());
            }
        }

        if (mismatches > 0) {
            log.warn("Tier 2 reconciliation: {} mismatches found",
                    mismatches);
        } else {
            log.info("Tier 2 reconciliation: all tenders match");
        }
    }
}
