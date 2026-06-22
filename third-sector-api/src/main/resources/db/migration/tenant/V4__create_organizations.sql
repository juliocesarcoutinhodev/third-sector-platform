CREATE TABLE IF NOT EXISTS organizations (
    id              UUID            PRIMARY KEY DEFAULT uuidv7(),
    name            VARCHAR(255)    NOT NULL,
    cnpj            VARCHAR(14)     NOT NULL UNIQUE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
