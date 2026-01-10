-- Convert deals.client_id to BINARY(16) (UUID) and ensure FK to clients(id)

-- 1) Change column type to BINARY(16)
ALTER TABLE deals MODIFY client_id BINARY(16) NULL;

-- 2) Add FK to clients(id) (drop if exists first to avoid duplicates)
-- MySQL 5.7 doesn't support IF EXISTS for foreign keys; we pick a clean name and add it.
-- If a constraint with the same name already exists, Flyway will stop and you can rename below.
ALTER TABLE deals ADD CONSTRAINT fk_deals_client FOREIGN KEY (client_id) REFERENCES clients(id);
