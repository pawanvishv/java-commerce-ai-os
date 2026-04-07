package com.commerceos.notification.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.notification.domain.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "channel", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;
}
