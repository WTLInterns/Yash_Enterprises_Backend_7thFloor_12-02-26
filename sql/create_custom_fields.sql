-- Create field_definitions table
CREATE TABLE IF NOT EXISTS field_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity VARCHAR(50) NOT NULL COMMENT 'bank, client, product, deal',
    field_key VARCHAR(100) NOT NULL COMMENT 'unique key for field',
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
