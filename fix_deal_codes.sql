-- Fix dealCode for existing deals that have NULL dealCode
-- Run this once in your MySQL database

UPDATE deals d
JOIN (
    SELECT id, department,
           CONCAT(UPPER(department), ROW_NUMBER() OVER (PARTITION BY department ORDER BY id)) AS new_code
    FROM deals
    WHERE deal_code IS NULL OR deal_code = ''
) ranked ON d.id = ranked.id
SET d.deal_code = ranked.new_code
WHERE d.deal_code IS NULL OR d.deal_code = '';

-- Verify
SELECT department, COUNT(*) as total, SUM(CASE WHEN deal_code IS NULL THEN 1 ELSE 0 END) as null_codes
FROM deals
GROUP BY department;
