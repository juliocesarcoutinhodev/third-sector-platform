CREATE TABLE IF NOT EXISTS municipality (
    id         UUID        PRIMARY KEY DEFAULT uuidv7(),
    slug       VARCHAR(63) NOT NULL UNIQUE,
    active     BOOLEAN     NOT NULL DEFAULT true,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
