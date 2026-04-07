package com.commerceos.authservice.infrastructure.scheduler;

import com.commerceos.authservice.domain.enums.CredentialStatus;
import com.commerceos.authservice.infrastructure.persistence.ApiCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CredentialRotationJob {

    private final ApiCredentialRepository credentialRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final int ROTATION_DAYS = 90;
    private static final int ADVANCE_NOTICE_DAYS = 14;

    /**
     * Credential rotation — Phase 5:
     * 90-day cycle, 7-day overlap window,
     * 14-day advance notification.
     * Runs daily at 6 AM.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void checkCredentialExpiry() {
        log.info("Credential rotation check started");

        Instant notifyThreshold = Instant.now()
                .plus(ADVANCE_NOTICE_DAYS, ChronoUnit.DAYS);
        Instant expiredThreshold = Instant.now();

        credentialRepository.findAll().forEach(cred -> {
            if (cred.getExpiresAt() == null) return;

            if (cred.getExpiresAt().isBefore(expiredThreshold)) {
                cred.setStatus(CredentialStatus.EXPIRED);
                credentialRepository.save(cred);
                log.warn("Credential expired: {}",
                        cred.getClientId());

            } else if (cred.getExpiresAt()
                    .isBefore(notifyThreshold)) {
                log.warn("Credential expiring soon: {} on {}",
                        cred.getClientId(), cred.getExpiresAt());
            }
        });

        log.info("Credential rotation check completed");
    }
}
