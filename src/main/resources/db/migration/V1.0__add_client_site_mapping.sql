-- Add client_id to sites table for CRM structure
ALTER TABLE sites 
ADD COLUMN client_id BIGINT NOT NULL AFTER pincode;

-- Add foreign key constraint
ALTER TABLE sites 
ADD CONSTRAINT fk_sites_client_id 
FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE;

-- Add index for performance
CREATE INDEX idx_sites_client_id ON sites(client_id);

-- Create client_field_values table for dynamic custom fields
CREATE TABLE client_field_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    field_definition_id BIGINT NOT NULL,
    field_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    
    UNIQUE KEY uk_client_field_value (client_id, field_definition_id),
    
    CONSTRAINT fk_client_field_values_client 
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_client_field_values_definition 
    FOREIGN KEY (field_definition_id) REFERENCES client_field_definitions(id) ON DELETE CASCADE
);

-- Add index for performance
CREATE INDEX idx_client_field_values_client_id ON client_field_values(client_id);
CREATE INDEX idx_client_field_values_definition_id ON client_field_values(field_definition_id);

-- Add latitude and longitude columns to clients table if not exists
ALTER TABLE clients 
ADD COLUMN IF NOT EXISTS latitude DECIMAL(10, 8),
ADD COLUMN IF NOT EXISTS longitude DECIMAL(11, 8),
ADD COLUMN IF NOT EXISTS address VARCHAR(500),
ADD COLUMN IF NOT EXISTS city VARCHAR(150),
ADD COLUMN IF NOT EXISTS pincode VARCHAR(20),
ADD COLUMN IF NOT EXISTS state VARCHAR(150),
ADD COLUMN IF NOT EXISTS country VARCHAR(150),
ADD COLUMN IF NOT EXISTS contact_name VARCHAR(200),
ADD COLUMN IF NOT EXISTS contact_number VARCHAR(50),
ADD COLUMN IF NOT EXISTS country_code VARCHAR(10);

-- Add indexes for geocoding fields
CREATE INDEX idx_clients_lat_lng ON clients(latitude, longitude);
CREATE INDEX idx_clients_city ON clients(city);
