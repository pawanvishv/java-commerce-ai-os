package com.commerceos.notification.infrastructure.persistence;

import com.commerceos.notification.domain.model.NotificationLog;
import com.commerceos.notification.domain.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByTenantIdOrderByCreatedAtDesc(
            String tenantId);

    long countByStatus(NotificationStatus status);
}
