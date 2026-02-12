@echo off
mysql -h localhost -P 33060 -u root -padmin123 -e "ALTER TABLE customer_addresses MODIFY address_type VARCHAR(20) NOT NULL;"
echo Database fixed!
