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
public class PaymentCapturedConsumer {

    private final LedgerService ledgerService;

    @SuppressWarnings("unchecked")
    @KafkaListener(
            topics = "payment.captured.v1",
            groupId = "ledger-service")
    public void onPaymentCaptured(
            @Payload(required = false) Object rawEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("Ledger received payment.captured event offset: {}",
                offset);

        try {
            if (rawEvent == null) {
                log.warn("Null event — skipping");
                ack.acknowledge();
                return;
            }

            java.util.Map<String, Object> event =
                    (java.util.Map<String, Object>) rawEvent;

            String tenantId = String.valueOf(
                    event.getOrDefault("tenantId", "unknown"));
            String orderId = String.valueOf(
                    event.getOrDefault("orderId", "unknown"));

            Object amountObj = event.get("amountPaise");
            if (amountObj == null) {
                log.warn("Missing amountPaise — skipping");
                ack.acknowledge();
                return;
            }

            long amountPaise = Long.parseLong(amountObj.toString());
            long taxPaise = amountPaise / 5;

            ledgerService.postOrderPayment(
                    tenantId, orderId, amountPaise, taxPaise, orderId);

            ack.acknowledge();
            log.info("Ledger entries posted for order: {}", orderId);

        } catch (Exception e) {
            log.error("Failed to post ledger entry: {}",
                    e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
