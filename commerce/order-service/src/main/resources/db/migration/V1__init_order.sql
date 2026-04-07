CREATE TABLE IF NOT EXISTS orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    order_number    VARCHAR(100) NOT NULL UNIQUE,
    customer_id     VARCHAR(100),
    store_id        VARCHAR(100),
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_paise     BIGINT NOT NULL DEFAULT 0,
    tax_paise       BIGINT NOT NULL DEFAULT 0,
    channel         VARCHAR(20) NOT NULL DEFAULT 'ONLINE',
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_orders_tenant
    ON orders(tenant_id, created_at DESC);
CREATE INDEX idx_orders_customer
    ON orders(customer_id, tenant_id);
CREATE INDEX idx_orders_status
    ON orders(status, tenant_id);

CREATE TABLE IF NOT EXISTS order_lines (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id),
    tenant_id       VARCHAR(100) NOT NULL,
    sku             VARCHAR(100) NOT NULL,
    item_name       VARCHAR(255),
    item_type       VARCHAR(20) NOT NULL,
    qty             INT NOT NULL,
    unit_price_paise BIGINT NOT NULL,
    tax_paise       BIGINT NOT NULL DEFAULT 0,
    total_paise     BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_order_lines_order
    ON order_lines(order_id);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    order_id        UUID,
    response        TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_idempotency_order
    ON idempotency_keys(idempotency_key);

CREATE TABLE IF NOT EXISTS saga_state (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL UNIQUE,
    tenant_id       VARCHAR(100) NOT NULL,
    workflow_id     VARCHAR(255),
    status          VARCHAR(30) NOT NULL DEFAULT 'STARTED',
    current_step    VARCHAR(100),
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);
