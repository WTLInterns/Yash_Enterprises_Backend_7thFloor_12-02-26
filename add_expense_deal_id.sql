-- Add deal_id, stage_code, expense_type to expenses table
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS deal_id BIGINT NULL;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS stage_code VARCHAR(50) NULL;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS expense_type VARCHAR(20) NULL DEFAULT 'DEAL';

-- Backfill expense_type based on existing data
UPDATE expenses SET expense_type = CASE
  WHEN client_id IS NOT NULL AND department_name IS NOT NULL THEN 'DEAL'
  WHEN client_id IS NOT NULL THEN 'CLIENT'
  ELSE 'COMPANY'
END WHERE expense_type IS NULL;

-- Backfill deal_id from latest deal per client
UPDATE expenses e
SET deal_id = (
    SELECT d.id FROM deals d
    WHERE d.client_id = e.client_id
    ORDER BY d.created_at DESC, d.id DESC
    LIMIT 1
)
WHERE e.deal_id IS NULL AND e.client_id IS NOT NULL;

-- Backfill department_name from deal where missing/wrong
UPDATE expenses e
SET department_name = (
    SELECT d.department FROM deals d WHERE d.id = e.deal_id
)
WHERE e.deal_id IS NOT NULL AND (e.department_name IS NULL OR e.department_name = '');
