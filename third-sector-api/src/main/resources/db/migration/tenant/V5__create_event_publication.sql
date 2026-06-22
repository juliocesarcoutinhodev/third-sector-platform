CREATE TABLE IF NOT EXISTS event_publication (
    id                      UUID            PRIMARY KEY DEFAULT uuidv7(),
    publication_date        TIMESTAMPTZ     NOT NULL,
    listener_id             VARCHAR(512)    NOT NULL,
    serialized_event        TEXT            NOT NULL,
    event_type              VARCHAR(512)    NOT NULL,
    completion_date         TIMESTAMPTZ,
    last_resubmission_date  TIMESTAMPTZ,
    completion_attempts     INT             DEFAULT 0,
    status                  VARCHAR(50)
);
