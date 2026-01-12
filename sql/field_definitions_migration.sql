-- Create field_definitions table
CREATE TABLE IF NOT EXISTS field_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity VARCHAR(50) NOT NULL COMMENT 'bank, client, product, deal',
    field_key VARCHAR(100) NOT NULL COMMENT 'unique key for the field',
    field_name VARCHAR(255) NOT NULL COMMENT 'display name of the field',
    field_type ENUM('TEXT', 'NUMBER', 'DATE', 'SELECT', 'BOOLEAN', 'TEXTAREA') NOT NULL,
    required BOOLEAN DEFAULT FALSE,
    options_json JSON COMMENT 'for SELECT type fields',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE KEY unique_entity_field (entity, field_key),
    INDEX idx_entity_active (entity, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create field_values table
CREATE TABLE IF NOT EXISTS field_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity VARCHAR(50) NOT NULL COMMENT 'bank, client, product, deal',
    entity_id BIGINT NOT NULL COMMENT 'ID of the entity record',
    field_key VARCHAR(100) NOT NULL COMMENT 'key matching field_definitions.field_key',
    value TEXT COMMENT 'field value',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    UNIQUE KEY unique_entity_field_value (entity, entity_id, field_key),
    INDEX idx_entity_id (entity, entity_id),
    INDEX idx_field_key (field_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample data for testing
INSERT INTO field_definitions (entity, field_key, field_name, field_type, required) VALUES
('bank', 'account_number', 'Account Number', 'TEXT', false),
('bank', 'ifsc_code', 'IFSC Code', 'TEXT', true),
('bank', 'branch_code', 'Branch Code', 'TEXT', false),
('client', 'industry', 'Industry', 'SELECT', false),
('client', 'company_size', 'Company Size', 'SELECT', false),
('client', 'annual_revenue', 'Annual Revenue', 'NUMBER', false),
('product', 'sku', 'SKU', 'TEXT', true),
('product', 'weight', 'Weight', 'NUMBER', false),
('product', 'dimensions', 'Dimensions', 'TEXT', false),
('deal', 'probability', 'Probability (%)', 'NUMBER', false),
('deal', 'next_action', 'Next Action', 'TEXT', false),
('deal', 'expected_close_date', 'Expected Close Date', 'DATE', false);

-- Update options for SELECT fields
UPDATE field_definitions SET options_json = '["Technology", "Manufacturing", "Healthcare", "Finance", "Retail", "Education"]' WHERE entity = 'client' AND field_key = 'industry';
UPDATE field_definitions SET options_json = '["1-10", "11-50", "51-200", "201-500", "500+"]' WHERE entity = 'client' AND field_key = 'company_size';
