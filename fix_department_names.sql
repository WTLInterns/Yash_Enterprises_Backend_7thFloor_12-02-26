-- Fix department names to match proper stage departments
-- Update incorrect department names to proper ones

-- Update "ppo" to "PPO" (if it exists)
UPDATE departments SET name = 'PPO' WHERE name = 'ppo' OR name = 'PPO';

-- Update "pte" to "PTE" or proper department name (if it exists)
-- Check what "pte" should be - could be "PPE" or another proper department
UPDATE departments SET name = 'PPE' WHERE name = 'pte' OR name = 'PTE';

-- Ensure proper departments exist
INSERT IGNORE INTO departments (name, code, description, isActive, createdAt, updatedAt) VALUES
('PPE', 'PPE', 'Personal Protective Equipment', true, NOW(), NOW()),
('PPO', 'PPO', 'Post Purchase Offer', true, NOW(), NOW()),
('PSD', 'PSD', 'Property Sale Department', true, NOW(), NOW()),
('HLC', 'HLC', 'Home Loan Department', true, NOW(), NOW()),
('ROP', 'ROP', 'Rental of Property', true, NOW(), NOW());

-- Show current departments after update
SELECT * FROM departments ORDER BY name;
