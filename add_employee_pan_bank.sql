-- Add PAN and Bank Account fields to employees table
ALTER TABLE employees ADD COLUMN IF NOT EXISTS pan_number VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_account_number VARCHAR(50);
