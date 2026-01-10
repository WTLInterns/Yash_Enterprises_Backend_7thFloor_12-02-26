-- V103__product_categories_and_price_history.sql
-- MySQL compatible version (NO timezone types)

CREATE TABLE IF NOT EXISTS product_categories (
  id CHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_price_history (
  id CHAR(36) PRIMARY KEY,
  product_id CHAR(36) NOT NULL,
  old_price DECIMAL(15,2),
  new_price DECIMAL(15,2),
  changed_by CHAR(36),
  changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_product_price_history_product
    FOREIGN KEY (product_id)
    REFERENCES products(id)
    ON DELETE CASCADE
);
