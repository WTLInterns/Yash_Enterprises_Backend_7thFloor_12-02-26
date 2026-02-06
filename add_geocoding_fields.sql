-- Add geocoding and contact fields to clients table
ALTER TABLE clients ADD COLUMN latitude DOUBLE;
ALTER TABLE clients ADD COLUMN longitude DOUBLE;
ALTER TABLE clients ADD COLUMN city VARCHAR(255);
ALTER TABLE clients ADD COLUMN pincode VARCHAR(20);
ALTER TABLE clients ADD COLUMN state VARCHAR(255);
ALTER TABLE clients ADD COLUMN country VARCHAR(255);
ALTER TABLE clients ADD COLUMN contact_name VARCHAR(255);
ALTER TABLE clients ADD COLUMN contact_number VARCHAR(50);
ALTER TABLE clients ADD COLUMN country_code VARCHAR(10);
