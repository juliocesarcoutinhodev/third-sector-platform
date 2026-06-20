-- Replaces the minimal municipality table from V2 with the full production schema.
-- V2 had only id, slug, active, created_at — this adds name, cnpj, subdomain, plan, updated_at.
DROP TABLE IF EXISTS municipality;

CREATE TABLE municipality (
    id         BIGSERIAL     PRIMARY KEY,
    name       VARCHAR(255)  NOT NULL,
    cnpj       VARCHAR(18)   NOT NULL UNIQUE,
    subdomain  VARCHAR(63)   NOT NULL UNIQUE,
    plan       VARCHAR(50)   NOT NULL DEFAULT 'BASIC',
    active     BOOLEAN       NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
