-- Fix address_type column to support enum values
ALTER TABLE customer_addresses 
MODIFY address_type VARCHAR(20) NOT NULL;
