CREATE TABLE IF NOT EXISTS hsn_sac_slabs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(20) NOT NULL,
    code_type   VARCHAR(10) NOT NULL,
    description VARCHAR(255),
    cgst_rate   DECIMAL(5,2) NOT NULL DEFAULT 0,
    sgst_rate   DECIMAL(5,2) NOT NULL DEFAULT 0,
    igst_rate   DECIMAL(5,2) NOT NULL DEFAULT 0,
    cess_rate   DECIMAL(5,2) NOT NULL DEFAULT 0,
    effective_from  DATE NOT NULL,
    effective_to    DATE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    UNIQUE(code, code_type, effective_from)
);

CREATE INDEX idx_hsn_sac_code ON hsn_sac_slabs(code, code_type);

INSERT INTO hsn_sac_slabs
    (code, code_type, description, cgst_rate, sgst_rate, igst_rate, effective_from)
VALUES
    ('0101', 'HSN', 'Live horses', 0, 0, 0, '2017-07-01'),
    ('1001', 'HSN', 'Wheat and meslin', 0, 0, 0, '2017-07-01'),
    ('6101', 'HSN', 'Mens overcoats', 2.5, 2.5, 5, '2017-07-01'),
    ('6201', 'HSN', 'Womens overcoats', 2.5, 2.5, 5, '2017-07-01'),
    ('8471', 'HSN', 'Computers', 9, 9, 18, '2017-07-01'),
    ('8517', 'HSN', 'Mobile phones', 6, 6, 12, '2017-07-01'),
    ('9983', 'SAC', 'IT services', 9, 9, 18, '2017-07-01'),
    ('9984', 'SAC', 'Telecom services', 9, 9, 18, '2017-07-01'),
    ('9985', 'SAC', 'Support services', 9, 9, 18, '2017-07-01'),
    ('9987', 'SAC', 'Maintenance services', 9, 9, 18, '2017-07-01');
