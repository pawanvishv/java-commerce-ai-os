package com.commerceos.kafka.contracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KafkaEvent<T> {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String eventType;
    private String tenantId;
    private String traceId;

    @Builder.Default
    private Instant occurredAt = Instant.now();

    private T payload;

    public static <T> KafkaEvent<T> of(String eventType,
                                        String tenantId,
                                        String traceId,
                                        T payload) {
        return KafkaEvent.<T>builder()
                .eventType(eventType)
                .tenantId(tenantId)
                .traceId(traceId)
                .payload(payload)
                .build();
    }
}
