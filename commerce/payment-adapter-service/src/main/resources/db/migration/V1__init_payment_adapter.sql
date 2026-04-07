CREATE TABLE IF NOT EXISTS payment_tenders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key     VARCHAR(255) NOT NULL UNIQUE,
    order_id            VARCHAR(100) NOT NULL,
    tenant_id           VARCHAR(100) NOT NULL,
    gateway             VARCHAR(30) NOT NULL,
    gateway_order_id    VARCHAR(255),
    gateway_payment_id  VARCHAR(255),
    amount_paise        BIGINT NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'INR',
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    failure_reason      TEXT,
    raw_response        JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_payment_tenders_order
    ON payment_tenders(order_id, tenant_id);
CREATE INDEX idx_payment_tenders_status
    ON payment_tenders(status);
CREATE INDEX idx_payment_tenders_idempotency
    ON payment_tenders(idempotency_key);

CREATE TABLE IF NOT EXISTS payment_webhooks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gateway         VARCHAR(30) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB NOT NULL,
    processed       BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_payment_webhooks_gateway
    ON payment_webhooks(gateway, processed);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    response        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_idempotency_keys_key
    ON idempotency_keys(idempotency_key);
CREATE INDEX idx_idempotency_keys_expires
    ON idempotency_keys(expires_at);
