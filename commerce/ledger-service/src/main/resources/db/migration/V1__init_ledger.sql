CREATE TABLE IF NOT EXISTS ledger_entries (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    order_id        VARCHAR(100),
    entry_type      VARCHAR(30) NOT NULL,
    account_type    VARCHAR(50) NOT NULL,
    amount_paise    BIGINT NOT NULL,
    direction       VARCHAR(10) NOT NULL,
    reference_id    VARCHAR(100),
    reference_type  VARCHAR(50),
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE TABLE ledger_entries_2024
    PARTITION OF ledger_entries
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE ledger_entries_2025
    PARTITION OF ledger_entries
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

CREATE TABLE ledger_entries_2026
    PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE ledger_entries_2027
    PARTITION OF ledger_entries
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');

CREATE INDEX idx_ledger_tenant
    ON ledger_entries(tenant_id, created_at DESC);
CREATE INDEX idx_ledger_order
    ON ledger_entries(order_id, tenant_id);

CREATE OR REPLACE FUNCTION prevent_ledger_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Ledger entries are immutable';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ledger_immutable
    BEFORE UPDATE OR DELETE ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION prevent_ledger_modification();

CREATE TABLE IF NOT EXISTS gst_ledger (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    order_id        VARCHAR(100),
    gst_type        VARCHAR(20) NOT NULL,
    amount_paise    BIGINT NOT NULL,
    direction       VARCHAR(10) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE TABLE gst_ledger_2026
    PARTITION OF gst_ledger
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE gst_ledger_2027
    PARTITION OF gst_ledger
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');

CREATE INDEX idx_gst_ledger_tenant
    ON gst_ledger(tenant_id, created_at DESC);

CREATE TABLE IF NOT EXISTS reconciliation_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    order_id        VARCHAR(100),
    tier            VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    mismatch_amount BIGINT,
    detail          TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
