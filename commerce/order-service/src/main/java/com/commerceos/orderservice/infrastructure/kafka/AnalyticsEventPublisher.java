package com.commerceos.orderservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Analytics boundary — publishes events to analytics.* topics.
 * ClickHouse consumers will pick these up in Phase 5+.
 * This is a one-way boundary — analytics never writes back.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderAnalytics(String tenantId,
                                       String orderId,
                                       long totalPaise,
                                       int lineCount,
                                       String channel) {
        try {
            kafkaTemplate.send("analytics.order.v1", tenantId,
                    Map.of(
                            "tenantId", tenantId,
                            "orderId", orderId,
                            "totalPaise", totalPaise,
                            "lineCount", lineCount,
                            "channel", channel,
                            "occurredAt", Instant.now().toString()
                    ));
            log.debug("Analytics event published for order: {}",
                    orderId);
        } catch (Exception e) {
            log.warn("Failed to publish analytics event: {}",
                    e.getMessage());
        }
    }

    public void publishRevenueAnalytics(String tenantId,
                                         String period,
                                         long revenuePaise,
                                         long taxPaise,
                                         int orderCount) {
        try {
            kafkaTemplate.send("analytics.revenue.v1", tenantId,
                    Map.of(
                            "tenantId", tenantId,
                            "period", period,
                            "revenuePaise", revenuePaise,
                            "taxPaise", taxPaise,
                            "orderCount", orderCount,
                            "occurredAt", Instant.now().toString()
                    ));
        } catch (Exception e) {
            log.warn("Failed to publish revenue analytics: {}",
                    e.getMessage());
        }
    }
}
