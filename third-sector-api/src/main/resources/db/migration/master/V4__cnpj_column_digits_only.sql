-- Store CNPJ as 14 digits only (no formatting mask).
-- Formatting is done at the presentation layer (e.g. Angular pipe/mask).
ALTER TABLE municipality ALTER COLUMN cnpj TYPE VARCHAR(14);
