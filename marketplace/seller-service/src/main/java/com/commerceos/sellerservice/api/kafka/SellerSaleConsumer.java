package com.commerceos.sellerservice.api.kafka;

import com.commerceos.sellerservice.application.handlers.SellerService;
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
public class SellerSaleConsumer {

    private final SellerService sellerService;

    @SuppressWarnings("unchecked")
    @KafkaListener(
            topics = "seller.sale.v1",
            groupId = "seller-service")
    public void onSellerSale(
            @Payload(required = false) Object rawEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("Seller received sale event offset: {}", offset);

        try {
            if (rawEvent == null) {
                ack.acknowledge();
                return;
            }

            java.util.Map<String, Object> event =
                    (java.util.Map<String, Object>) rawEvent;

            String sellerId = String.valueOf(
                    event.getOrDefault("sellerId", ""));
            String tenantId = String.valueOf(
                    event.getOrDefault("tenantId", ""));
            String orderId = String.valueOf(
                    event.getOrDefault("orderId", ""));
            long salePaise = Long.parseLong(
                    event.getOrDefault("salePaise", 0).toString());

            if (!sellerId.isEmpty() && !tenantId.isEmpty()) {
                sellerService.processSale(
                        sellerId, tenantId, orderId, salePaise);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process seller sale: {}",
                    e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
