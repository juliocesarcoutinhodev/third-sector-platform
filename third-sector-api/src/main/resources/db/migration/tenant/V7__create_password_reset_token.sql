CREATE TABLE IF NOT EXISTS password_reset_token (
    id              UUID            PRIMARY KEY DEFAULT uuidv7(),
    user_id         UUID            NOT NULL,
    token_hash      VARCHAR(64)     NOT NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used            BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_hash ON password_reset_token (token_hash);
