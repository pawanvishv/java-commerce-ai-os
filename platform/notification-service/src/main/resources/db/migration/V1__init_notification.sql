CREATE TABLE IF NOT EXISTS notification_templates (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(100) NOT NULL UNIQUE,
    channel     VARCHAR(20) NOT NULL,
    subject     VARCHAR(255),
    body        TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS notification_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100),
    recipient       VARCHAR(255) NOT NULL,
    channel         VARCHAR(20) NOT NULL,
    template_code   VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message   TEXT,
    retry_count     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at         TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_notification_log_tenant
    ON notification_log(tenant_id, created_at DESC);

CREATE INDEX idx_notification_log_status
    ON notification_log(status);

INSERT INTO notification_templates (code, channel, subject, body)
VALUES
    ('CLIENT_REGISTERED', 'EMAIL',
     'Welcome to Commerce OS',
     'Dear {{name}}, your account has been registered successfully.'),
    ('TENANT_ACTIVATED', 'EMAIL',
     'Your account is now active',
     'Dear {{name}}, your Commerce OS account is now active.'),
    ('TENANT_SUSPENDED', 'EMAIL',
     'Your account has been suspended',
     'Dear {{name}}, your account has been suspended. Please contact support.');
