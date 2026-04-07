CREATE TABLE IF NOT EXISTS sellers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(100) NOT NULL,
    seller_id           VARCHAR(100) NOT NULL UNIQUE,
    business_name       VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    phone               VARCHAR(20),
    pan                 VARCHAR(20),
    gstin               VARCHAR(20),
    bank_account        VARCHAR(50),
    bank_ifsc           VARCHAR(20),
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING_KYC',
    rating              DECIMAL(3,2) DEFAULT 5.00,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_sellers_tenant ON sellers(tenant_id);
CREATE INDEX idx_sellers_status ON sellers(status);

CREATE TABLE IF NOT EXISTS seller_commission_rates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id       VARCHAR(100) NOT NULL,
    tenant_id       VARCHAR(100) NOT NULL,
    category        VARCHAR(100),
    rate_percent    DECIMAL(5,2) NOT NULL,
    effective_from  TIMESTAMPTZ NOT NULL DEFAULT now(),
    approved_by     VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_commission_seller
    ON seller_commission_rates(seller_id, tenant_id);

CREATE TABLE IF NOT EXISTS seller_payouts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id       VARCHAR(100) NOT NULL,
    tenant_id       VARCHAR(100) NOT NULL,
    amount_paise    BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reference_id    VARCHAR(100),
    failure_reason  TEXT,
    retry_count     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_payouts_seller
    ON seller_payouts(seller_id, tenant_id);
CREATE INDEX idx_payouts_status
    ON seller_payouts(status);

CREATE TABLE IF NOT EXISTS seller_tcs_ledger (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    seller_id       VARCHAR(100) NOT NULL,
    tenant_id       VARCHAR(100) NOT NULL,
    order_id        VARCHAR(100) NOT NULL,
    tcs_amount_paise BIGINT NOT NULL,
    direction       VARCHAR(10) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE TABLE seller_tcs_ledger_2026
    PARTITION OF seller_tcs_ledger
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE seller_tcs_ledger_2027
    PARTITION OF seller_tcs_ledger
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');

CREATE TABLE IF NOT EXISTS seller_kyc_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id       VARCHAR(100) NOT NULL UNIQUE,
    tenant_id       VARCHAR(100) NOT NULL,
    workflow_id     VARCHAR(255),
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    current_step    VARCHAR(100),
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS seller_audit_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id   VARCHAR(100) NOT NULL,
    tenant_id   VARCHAR(100) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    detail      TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_seller_audit
    ON seller_audit_log(seller_id, created_at DESC);
