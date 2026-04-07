package com.commerceos.platformadmin.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataArchivalJob {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Archive processed outbox events older than 7 days.
     * Runs daily at 1 AM.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void archiveOutboxEvents() {
        log.info("Data archival job started");
        try {
            int deleted = jdbcTemplate.update("""
                DELETE FROM outbox_events
                WHERE status IN ('PROCESSED', 'DEAD')
                AND created_at < now() - INTERVAL '7 days'
                """);
            log.info("Archived {} outbox events", deleted);
        } catch (Exception e) {
            log.error("Archival failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Archive old audit logs older than 90 days.
     * Runs weekly on Sunday at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void archiveAuditLogs() {
        log.info("Audit log archival started");
        try {
            int deleted = jdbcTemplate.update("""
                DELETE FROM admin_audit_log
                WHERE created_at < now() - INTERVAL '90 days'
                """);
            log.info("Archived {} audit log entries", deleted);
        } catch (Exception e) {
            log.error("Audit archival failed: {}",
                    e.getMessage(), e);
        }
    }
}
