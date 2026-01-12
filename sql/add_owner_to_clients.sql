-- Add owner_id column to clients table
ALTER TABLE clients ADD COLUMN owner_id BIGINT;

-- Add foreign key constraint for owner_id (optional)
-- ALTER TABLE clients ADD CONSTRAINT fk_client_owner FOREIGN KEY (owner_id) REFERENCES employee(id);
