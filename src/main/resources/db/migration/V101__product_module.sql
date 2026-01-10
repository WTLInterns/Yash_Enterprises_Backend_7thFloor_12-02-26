-- Product master
CREATE TABLE IF NOT EXISTS products (
  id CHAR(36) PRIMARY KEY,
  product_name VARCHAR(255) NOT NULL UNIQUE,
  product_code VARCHAR(128),
  product_category VARCHAR(255),
  unit_price DECIMAL(15,2),
  description TEXT,
  active BOOLEAN,
  owner_id CHAR(36),
  created_at DATETIME(6),
  updated_at DATETIME(6)
);

SET @idx_products_name := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'products'
    AND index_name = 'idx_products_name'
);
SET @sql_products_name := IF(@idx_products_name = 0,
  'CREATE INDEX idx_products_name ON products(product_name)',
  'SELECT 1'
);
PREPARE stmt_products_name FROM @sql_products_name;
EXECUTE stmt_products_name;
DEALLOCATE PREPARE stmt_products_name;

SET @idx_products_code := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'products'
    AND index_name = 'idx_products_code'
);
SET @sql_products_code := IF(@idx_products_code = 0,
  'CREATE INDEX idx_products_code ON products(product_code)',
  'SELECT 1'
);
PREPARE stmt_products_code FROM @sql_products_code;
EXECUTE stmt_products_code;
DEALLOCATE PREPARE stmt_products_code;

-- Dynamic field definitions
CREATE TABLE IF NOT EXISTS product_field_definitions (
  id CHAR(36) PRIMARY KEY,
  field_name VARCHAR(255) NOT NULL,
  field_key VARCHAR(255) NOT NULL UNIQUE,
  field_type VARCHAR(32) NOT NULL,
  required BOOLEAN,
  active BOOLEAN
);

-- Field values (EAV)
CREATE TABLE IF NOT EXISTS product_field_values (
  id CHAR(36) PRIMARY KEY,
  product_id CHAR(36) NOT NULL,
  field_definition_id CHAR(36) NOT NULL,
  value TEXT,
  CONSTRAINT uk_product_field_value_unique UNIQUE (product_id, field_definition_id)
);

ALTER TABLE product_field_values
  ADD CONSTRAINT fk_product_field_values_product
  FOREIGN KEY (product_id) REFERENCES products(id)
  ON DELETE CASCADE;

ALTER TABLE product_field_values
  ADD CONSTRAINT fk_product_field_values_definition
  FOREIGN KEY (field_definition_id) REFERENCES product_field_definitions(id)
  ON DELETE CASCADE;

-- Deal to product attachment (snapshot pricing)
CREATE TABLE IF NOT EXISTS deal_products (
  id CHAR(36) PRIMARY KEY,
  deal_id CHAR(36) NOT NULL,
  product_id CHAR(36) NOT NULL,
  unit_price DECIMAL(15,2),
  quantity DECIMAL(15,2),
  discount DECIMAL(15,2),
  tax DECIMAL(15,2),
  total DECIMAL(15,2),
  created_at DATETIME(6),
  updated_at DATETIME(6)
);

ALTER TABLE deal_products
  ADD CONSTRAINT fk_deal_products_deal
  FOREIGN KEY (deal_id) REFERENCES deals(id)
  ON DELETE CASCADE;

ALTER TABLE deal_products
  ADD CONSTRAINT fk_deal_products_product
  FOREIGN KEY (product_id) REFERENCES products(id);
