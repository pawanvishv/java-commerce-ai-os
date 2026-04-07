package com.commerceos.notification.infrastructure.persistence;

import com.commerceos.notification.domain.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository
        extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByCode(String code);
}
