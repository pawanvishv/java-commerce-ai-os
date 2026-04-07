package com.commerceos.common.exception;

public class IdempotencyException extends BusinessException {

    public IdempotencyException(String key) {
        super("IDEMPOTENCY_CONFLICT", "Request already processed with key: " + key, 409);
    }
}
