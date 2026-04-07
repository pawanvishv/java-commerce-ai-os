CREATE TABLE IF NOT EXISTS slot_bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    item_id         VARCHAR(100) NOT NULL,
    slot_date       DATE NOT NULL,
    slot_time       TIME NOT NULL,
    available_count INT NOT NULL DEFAULT 0,
    reserved_count  INT NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    UNIQUE(tenant_id, item_id, slot_date, slot_time)
);

CREATE INDEX idx_slot_bookings_tenant
    ON slot_bookings(tenant_id, item_id, slot_date);

CREATE TABLE IF NOT EXISTS pending_slot_reservations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    slot_id     UUID NOT NULL REFERENCES slot_bookings(id),
    order_id    VARCHAR(100) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE INDEX idx_pending_slot_order
    ON pending_slot_reservations(order_id, tenant_id);
