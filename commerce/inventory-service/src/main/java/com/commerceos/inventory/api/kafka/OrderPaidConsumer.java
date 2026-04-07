package com.commerceos.inventory.api.kafka;

import com.commerceos.inventory.application.handlers.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidConsumer {

    private final InventoryService inventoryService;

    @SuppressWarnings("unchecked")
    @KafkaListener(
            topics = "order.paid.v1",
            groupId = "inventory-service")
    public void onOrderPaid(
            @Payload(required = false) Object rawEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("Inventory received order.paid.v1 offset: {}",
                offset);

        try {
            if (rawEvent == null) {
                ack.acknowledge();
                return;
            }

            java.util.Map<String, Object> event =
                    (java.util.Map<String, Object>) rawEvent;

            String tenantId = String.valueOf(
                    event.getOrDefault("tenantId", "unknown"));
            String orderId = String.valueOf(
                    event.getOrDefault("orderId", "unknown"));

            log.info("Order paid — inventory confirmation " +
                    "pending for order: {}", orderId);

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process order.paid in inventory: {}",
                    e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
