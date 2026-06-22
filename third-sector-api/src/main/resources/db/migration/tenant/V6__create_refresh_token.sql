CREATE TABLE IF NOT EXISTS refresh_token (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    token_hash      VARCHAR(64)     NOT NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    family_id       VARCHAR(36)     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_hash ON refresh_token (token_hash);
