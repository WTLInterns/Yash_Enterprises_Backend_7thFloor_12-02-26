-- ============================================================
-- Add INVENTORY stage before ACCOUNT in all departments
-- PPO, PPE, PSD, HLC, ROP
-- ============================================================

-- STEP 1: Shift ACCOUNT stage order up by 1 in all departments
UPDATE stages SET stage_order = stage_order + 1
WHERE stage_code = 'ACCOUNT'
  AND department IN ('PPO', 'PPE', 'PSD', 'HLC', 'ROP');

-- STEP 2: Insert INVENTORY stage before ACCOUNT in each department

-- PPO (ACCOUNT was order 7, now 8 → INVENTORY = 7)
INSERT INTO stages (stage_name, stage_code, department, stage_order, is_terminal)
VALUES ('Inventory', 'INVENTORY', 'PPO', 7, false)
ON CONFLICT DO NOTHING;

-- PPE (ACCOUNT was order 7, now 8 → INVENTORY = 7)
INSERT INTO stages (stage_name, stage_code, department, stage_order, is_terminal)
VALUES ('Inventory', 'INVENTORY', 'PPE', 7, false)
ON CONFLICT DO NOTHING;

-- PSD (ACCOUNT was order 7, now 8 → INVENTORY = 7)
INSERT INTO stages (stage_name, stage_code, department, stage_order, is_terminal)
VALUES ('Inventory', 'INVENTORY', 'PSD', 7, false)
ON CONFLICT DO NOTHING;

-- HLC (ACCOUNT was order 7, now 8 → INVENTORY = 7)
INSERT INTO stages (stage_name, stage_code, department, stage_order, is_terminal)
VALUES ('Inventory', 'INVENTORY', 'HLC', 7, false)
ON CONFLICT DO NOTHING;

-- ROP (ACCOUNT was order 7, now 8 → INVENTORY = 7)
INSERT INTO stages (stage_name, stage_code, department, stage_order, is_terminal)
VALUES ('Inventory', 'INVENTORY', 'ROP', 7, false)
ON CONFLICT DO NOTHING;

-- ============================================================
-- VERIFY: Check all stages after update
-- ============================================================
SELECT department, stage_order, stage_name, stage_code, is_terminal
FROM stages
WHERE department IN ('PPO', 'PPE', 'PSD', 'HLC', 'ROP')
ORDER BY department, stage_order;

-- Expected result for each dept:
-- order 1-6: existing stages
-- order 7: Inventory (INVENTORY) - is_terminal: false
-- order 8: Account  (ACCOUNT)    - is_terminal: true
