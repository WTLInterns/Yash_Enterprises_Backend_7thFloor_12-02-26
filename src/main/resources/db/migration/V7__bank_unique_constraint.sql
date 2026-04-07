-- Step 1: Delete duplicate banks, keeping only the lowest id per (name, branch_name)
-- First reassign any deals/references to the surviving bank id
UPDATE deals d
JOIN banks b_dup ON d.bank_id = b_dup.id
JOIN (
    SELECT MIN(id) AS keep_id, name, branch_name
    FROM banks
    GROUP BY name, branch_name
) b_keep ON LOWER(b_dup.name) = LOWER(b_keep.name)
         AND (
             (b_dup.branch_name IS NULL AND b_keep.branch_name IS NULL)
             OR LOWER(b_dup.branch_name) = LOWER(b_keep.branch_name)
         )
SET d.bank_id = b_keep.keep_id
WHERE b_dup.id != b_keep.keep_id;

-- Step 2: Soft-delete the duplicate banks (keep lowest id per name+branch)
UPDATE banks b_dup
JOIN (
    SELECT MIN(id) AS keep_id, name, branch_name
    FROM banks
    GROUP BY name, branch_name
) b_keep ON LOWER(b_dup.name) = LOWER(b_keep.name)
         AND (
             (b_dup.branch_name IS NULL AND b_keep.branch_name IS NULL)
             OR LOWER(b_dup.branch_name) = LOWER(b_keep.branch_name)
         )
SET b_dup.is_active = 0
WHERE b_dup.id != b_keep.keep_id;

-- Step 3: Hard-delete the inactive duplicates that were just created by dedup
DELETE FROM banks
WHERE is_active = 0
  AND id NOT IN (
      SELECT keep_id FROM (
          SELECT MIN(id) AS keep_id FROM banks GROUP BY name, branch_name
      ) AS t
  );

-- Step 4: Add unique constraint on (name, branch_name)
ALTER TABLE banks
    ADD CONSTRAINT uk_bank_name_branch UNIQUE (name, branch_name);
