package com.commerceos.notification.api.kafka;

import com.commerceos.notification.application.commands.SendNotificationCommand;
import com.commerceos.notification.application.handlers.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "notification.send.v1",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onNotificationSend(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.debug("Received notification event from {}:{}:{}",
                topic, partition, offset);

        try {
            Object payload = event.get("payload");
            if (payload instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) payload;

                SendNotificationCommand cmd = SendNotificationCommand.builder()
                        .tenantId((String) event.get("tenantId"))
                        .recipient((String) data.get("recipient"))
                        .channel((String) data.getOrDefault("channel", "EMAIL"))
                        .templateCode((String) data.get("templateCode"))
                        .subject((String) data.get("subject"))
                        .body((String) data.get("body"))
                        .build();

                notificationService.send(cmd);
            }
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process notification event: {}",
                    e.getMessage());
            ack.acknowledge();
        }
    }
}
