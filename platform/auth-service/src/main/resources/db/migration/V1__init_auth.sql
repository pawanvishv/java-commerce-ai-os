CREATE TABLE IF NOT EXISTS api_credentials (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id           VARCHAR(100) NOT NULL UNIQUE,
    client_secret_hash  VARCHAR(255) NOT NULL,
    tenant_id           VARCHAR(100) NOT NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    scopes              TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at          TIMESTAMPTZ,
    last_rotated_at     TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_api_credentials_tenant_id ON api_credentials(tenant_id);
CREATE INDEX idx_api_credentials_client_id ON api_credentials(client_id);

CREATE TABLE IF NOT EXISTS revoked_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jti         VARCHAR(255) NOT NULL UNIQUE,
    tenant_id   VARCHAR(100) NOT NULL,
    revoked_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_revoked_tokens_jti ON revoked_tokens(jti);

CREATE TABLE IF NOT EXISTS auth_audit_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100),
    client_id   VARCHAR(100),
    action      VARCHAR(100) NOT NULL,
    detail      TEXT,
    ip_address  VARCHAR(50),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auth_audit_tenant ON auth_audit_log(tenant_id, created_at DESC);
