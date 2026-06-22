CREATE TABLE IF NOT EXISTS isolation_record (
    id   UUID        PRIMARY KEY DEFAULT uuidv7(),
    data VARCHAR(255) NOT NULL
);
