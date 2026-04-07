package com.commerceos.notification.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.notification.domain.enums.NotificationChannel;
import com.commerceos.notification.domain.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notification_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog extends BaseEntity {

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "channel", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Column(name = "template_code")
    private String templateCode;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "sent_at")
    private Instant sentAt;
}
