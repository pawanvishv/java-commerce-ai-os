CREATE TABLE IF NOT EXISTS provisioning_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL UNIQUE,
    workflow_id     VARCHAR(255),
    profile         VARCHAR(50) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    current_step    VARCHAR(100),
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_provisioning_tenant ON provisioning_jobs(tenant_id);
CREATE INDEX idx_provisioning_status ON provisioning_jobs(status);

CREATE TABLE IF NOT EXISTS provisioning_steps (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id          UUID NOT NULL REFERENCES provisioning_jobs(id),
    step_name       VARCHAR(100) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    error_message   TEXT
);

CREATE INDEX idx_provisioning_steps_job ON provisioning_steps(job_id);
