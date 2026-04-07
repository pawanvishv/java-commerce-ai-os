package com.commerceos.ledger.api.kafka;

import com.commerceos.ledger.application.handlers.LedgerService;
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

    private final LedgerService ledgerService;

    @SuppressWarnings("unchecked")
    @KafkaListener(
            topics = "order.paid.v1",
            groupId = "ledger-service")
    public void onOrderPaid(
            @Payload(required = false) Object rawEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("Ledger received order.paid.v1 offset: {}", offset);

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
            long totalPaise = Long.parseLong(
                    event.getOrDefault("totalPaise", 0).toString());
            long taxPaise = totalPaise / 5;

            ledgerService.postOrderPayment(
                    tenantId, orderId, totalPaise, taxPaise, orderId);

            ack.acknowledge();
            log.info("Ledger entries posted for order.paid: {}",
                    orderId);

        } catch (Exception e) {
            log.error("Failed to process order.paid: {}",
                    e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
