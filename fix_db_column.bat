@echo off
mysql -h localhost -P 33060 -u root -padmin123 -e "ALTER TABLE customer_addresses CHANGE address_text address_line VARCHAR(255) NOT NULL;"
echo Database column name fixed!
