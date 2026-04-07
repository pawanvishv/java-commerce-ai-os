CREATE TABLE IF NOT EXISTS store_inventory (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    store_id        VARCHAR(100) NOT NULL,
    sku             VARCHAR(100) NOT NULL,
    available_qty   BIGINT NOT NULL DEFAULT 0,
    reserved_qty    BIGINT NOT NULL DEFAULT 0,
    damaged_qty     BIGINT NOT NULL DEFAULT 0,
    expired_qty     BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    UNIQUE(tenant_id, store_id, sku),
    CONSTRAINT chk_available_qty CHECK (available_qty >= 0),
    CONSTRAINT chk_reserved_qty CHECK (reserved_qty >= 0)
);

CREATE INDEX idx_inventory_tenant
    ON store_inventory(tenant_id, store_id);
CREATE INDEX idx_inventory_sku
    ON store_inventory(sku, tenant_id);

CREATE TABLE IF NOT EXISTS inventory_movements (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    store_id        VARCHAR(100) NOT NULL,
    sku             VARCHAR(100) NOT NULL,
    movement_type   VARCHAR(30) NOT NULL,
    qty             BIGINT NOT NULL,
    reference_id    VARCHAR(100),
    reference_type  VARCHAR(50),
    batch_id        VARCHAR(100),
    note            TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE TABLE inventory_movements_2026
    PARTITION OF inventory_movements
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE inventory_movements_2027
    PARTITION OF inventory_movements
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');

CREATE INDEX idx_movements_tenant
    ON inventory_movements(tenant_id, created_at DESC);
CREATE INDEX idx_movements_sku
    ON inventory_movements(sku, store_id, tenant_id);

CREATE TABLE IF NOT EXISTS pending_reservations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    store_id        VARCHAR(100) NOT NULL,
    sku             VARCHAR(100) NOT NULL,
    order_id        VARCHAR(100) NOT NULL,
    qty             BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_pending_reservations_order
    ON pending_reservations(order_id, tenant_id);
CREATE INDEX idx_pending_reservations_expires
    ON pending_reservations(expires_at)
    WHERE status = 'PENDING';

CREATE TABLE IF NOT EXISTS inventory_batches (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    store_id        VARCHAR(100) NOT NULL,
    sku             VARCHAR(100) NOT NULL,
    batch_number    VARCHAR(100) NOT NULL,
    qty             BIGINT NOT NULL DEFAULT 0,
    expiry_date     DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    UNIQUE(tenant_id, store_id, sku, batch_number)
);

CREATE INDEX idx_batches_expiry
    ON inventory_batches(tenant_id, sku, expiry_date ASC);
