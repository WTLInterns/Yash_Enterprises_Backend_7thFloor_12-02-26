-- Add JSON columns for custom fields
ALTER TABLE banks ADD COLUMN IF NOT EXISTS custom_fields JSON;
ALTER TABLE products ADD COLUMN IF NOT EXISTS custom_fields JSON;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS custom_fields JSON;
