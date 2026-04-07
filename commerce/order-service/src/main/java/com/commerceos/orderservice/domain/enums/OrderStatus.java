package com.commerceos.orderservice.domain.enums;

public enum OrderStatus {
    PENDING,
    RESERVED,
    PAYMENT_PENDING,
    PAID,
    PROCESSING,
    DELIVERED,
    CANCELLED,
    FAILED,
    REFUNDED
}
