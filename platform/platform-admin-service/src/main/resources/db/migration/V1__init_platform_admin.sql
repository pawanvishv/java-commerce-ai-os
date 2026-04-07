-- =========================
-- CLIENTS
-- =========================
CREATE TABLE IF NOT EXISTS clients (
                                       id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(100) NOT NULL UNIQUE,
    slug                VARCHAR(100) NOT NULL UNIQUE,
    business_name       VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    phone               VARCHAR(20),
    gstin               VARCHAR(20),
    pan                 VARCHAR(20),
    profile             VARCHAR(50) NOT NULL DEFAULT 'BILLING_ONLY',
    status              VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    plan                VARCHAR(50),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_clients_tenant_id ON clients(tenant_id);
CREATE INDEX IF NOT EXISTS idx_clients_status ON clients(status);
CREATE INDEX IF NOT EXISTS idx_clients_slug ON clients(slug);

-- =========================
-- CLIENT CONTACTS
-- =========================
CREATE TABLE IF NOT EXISTS client_contacts (
                                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id   UUID NOT NULL,
    type        VARCHAR(30) NOT NULL,
    value       VARCHAR(255) NOT NULL,
    verified    BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_client_contacts_client
    FOREIGN KEY (client_id)
    REFERENCES clients(id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_client_contacts_client ON client_contacts(client_id);

-- =========================
-- STORE SETTINGS
-- =========================
CREATE TABLE IF NOT EXISTS store_settings (
                                              id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    store_id    VARCHAR(100) NOT NULL,
    key         VARCHAR(100) NOT NULL,
    value       TEXT,
    scope       VARCHAR(20) NOT NULL DEFAULT 'GLOBAL',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE(tenant_id, store_id, key)
    );

CREATE INDEX IF NOT EXISTS idx_store_settings_tenant ON store_settings(tenant_id);

-- =========================
-- FEATURE FLAGS
-- =========================
CREATE TABLE IF NOT EXISTS feature_flags (
                                             id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    flag_key    VARCHAR(100) NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE(tenant_id, flag_key)
    );

CREATE INDEX IF NOT EXISTS idx_feature_flags_tenant ON feature_flags(tenant_id);

-- =========================
-- OUTBOX EVENTS
-- =========================
CREATE TABLE IF NOT EXISTS outbox_events (
                                             id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at    TIMESTAMPTZ,
    error_message   TEXT
    );

CREATE INDEX IF NOT EXISTS idx_outbox_status_created
    ON outbox_events(status, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_events(created_at)
    WHERE status = 'PENDING';

-- =========================
-- QUOTA COUNTERS
-- =========================
CREATE TABLE IF NOT EXISTS quota_counters (
                                              id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    metric      VARCHAR(100) NOT NULL,
    window_type VARCHAR(50) NOT NULL,
    count       BIGINT NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE(tenant_id, metric, window_type)
    );

-- =========================
-- ADMIN AUDIT LOG
-- =========================
CREATE TABLE IF NOT EXISTS admin_audit_log (
                                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    payload     JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_admin_audit_tenant
    ON admin_audit_log(tenant_id);

-- =========================
-- PROVISIONING JOBS
-- =========================
CREATE TABLE IF NOT EXISTS provisioning_jobs (
                                                 id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    workflow_id VARCHAR(255),
    status      VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    profile     VARCHAR(50) NOT NULL,
    step        VARCHAR(100),
    error       TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_provisioning_tenant
    ON provisioning_jobs(tenant_id);