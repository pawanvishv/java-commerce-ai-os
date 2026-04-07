package com.commerceos.notification.application.handlers;

import com.commerceos.notification.application.commands.SendNotificationCommand;
import com.commerceos.notification.domain.enums.NotificationChannel;
import com.commerceos.notification.domain.enums.NotificationStatus;
import com.commerceos.notification.domain.model.NotificationLog;
import com.commerceos.notification.domain.model.NotificationTemplate;
import com.commerceos.notification.infrastructure.persistence.NotificationLogRepository;
import com.commerceos.notification.infrastructure.persistence.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;

    @Value("${notification.email.from:noreply@commerce.local}")
    private String fromEmail;

    @Value("${notification.channels.email-enabled:true}")
    private boolean emailEnabled;

    public void send(SendNotificationCommand cmd) {
        NotificationLog notifLog = NotificationLog.builder()
                .tenantId(cmd.getTenantId())
                .recipient(cmd.getRecipient())
                .channel(NotificationChannel.valueOf(
                        cmd.getChannel() != null
                                ? cmd.getChannel() : "EMAIL"))
                .templateCode(cmd.getTemplateCode())
                .build();

        try {
            String subject = cmd.getSubject();
            String body = cmd.getBody();

            if (cmd.getTemplateCode() != null) {
                NotificationTemplate template = templateRepository
                        .findByCode(cmd.getTemplateCode())
                        .orElse(null);

                if (template != null) {
                    subject = resolveVariables(
                            template.getSubject(), cmd.getVariables());
                    body = resolveVariables(
                            template.getBody(), cmd.getVariables());
                }
            }

            if (emailEnabled && "EMAIL".equals(cmd.getChannel())) {
                sendEmail(cmd.getRecipient(), subject, body);
            }

            notifLog.setStatus(NotificationStatus.SENT);
            notifLog.setSentAt(Instant.now());
            log.info("Notification sent to: {} via {}",
                    cmd.getRecipient(), cmd.getChannel());

        } catch (Exception e) {
            log.error("Failed to send notification to {}: {}",
                    cmd.getRecipient(), e.getMessage());
            notifLog.setStatus(NotificationStatus.FAILED);
            notifLog.setErrorMessage(e.getMessage());
            notifLog.setRetryCount(notifLog.getRetryCount() + 1);
        }

        logRepository.save(notifLog);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject != null ? subject : "Notification");
        message.setText(body != null ? body : "");
        mailSender.send(message);
    }

    private String resolveVariables(String template,
                                     Map<String, String> variables) {
        if (template == null || variables == null) return template;
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace(
                    "{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
