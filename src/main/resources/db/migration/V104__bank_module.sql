-- V104__bank_module.sql
-- MySQL compatible bank master module

CREATE TABLE IF NOT EXISTS banks (
  id CHAR(36) PRIMARY KEY,
  bank_name VARCHAR(255) NOT NULL UNIQUE,
  branch_name VARCHAR(255),
  phone VARCHAR(50),
  website VARCHAR(255),
  description TEXT,
  address VARCHAR(500),
  taluka VARCHAR(255),
  district VARCHAR(255),
  pin_code VARCHAR(20),
  owner_id CHAR(36),
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
);
