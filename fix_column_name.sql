-- Fix column name mismatch: address_text -> address_line
ALTER TABLE customer_addresses 
CHANGE address_text address_line VARCHAR(255) NOT NULL;
