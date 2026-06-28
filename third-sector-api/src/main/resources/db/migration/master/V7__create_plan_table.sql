-- Creates the plan table with configurable limits and replaces the static Plan enum
-- on municipality with a foreign key to plan.

CREATE TABLE plan (
    id                UUID         PRIMARY KEY DEFAULT uuidv7(),
    name              VARCHAR(50)  NOT NULL UNIQUE,
    max_organizations INTEGER      NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Seed the three catalog plans
INSERT INTO plan (id, name, max_organizations)
VALUES
    (uuidv7(), 'BASIC',         5),
    (uuidv7(), 'INTERMEDIATE', 20),
    (uuidv7(), 'ENTERPRISE',   NULL);

-- Add plan_id column to municipality (nullable initially for migration)
ALTER TABLE municipality
    ADD COLUMN plan_id UUID REFERENCES plan(id);

-- Map old enum values to new plan names before migrating
UPDATE municipality SET plan = 'INTERMEDIATE' WHERE plan = 'STANDARD';
UPDATE municipality SET plan = 'ENTERPRISE'   WHERE plan = 'PREMIUM';

-- Migrate data: map plan name string to new plan FK
UPDATE municipality m
SET plan_id = p.id
FROM plan p
WHERE p.name = m.plan;

-- Make plan_id NOT NULL after data migration
ALTER TABLE municipality
    ALTER COLUMN plan_id SET NOT NULL;

-- Drop the old plan column
ALTER TABLE municipality
    DROP COLUMN plan;
