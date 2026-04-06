-- SQL Migration: Update lead_closure_approvals table
-- Run this if table already exists, otherwise create fresh

-- Add missing columns (safe - won't fail if column exists)
ALTER TABLE lead_closure_approvals 
  ADD COLUMN IF NOT EXISTS deal_name VARCHAR(255),
  ADD COLUMN IF NOT EXISTS client_id BIGINT,
  ADD COLUMN IF NOT EXISTS current_stage VARCHAR(100),
  ADD COLUMN IF NOT EXISTS current_department VARCHAR(100),
  ADD COLUMN IF NOT EXISTS from_department VARCHAR(100),
  ADD COLUMN IF NOT EXISTS requested_by_name VARCHAR(255),
  ADD COLUMN IF NOT EXISTS reviewed_by_name VARCHAR(255),
  ADD COLUMN IF NOT EXISTS value_amount DECIMAL(15, 2),
  ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

-- If table doesn't exist, create it fresh:
CREATE TABLE IF NOT EXISTS lead_closure_approvals (
    id                    BIGSERIAL PRIMARY KEY,
    deal_id               BIGINT NOT NULL,
    deal_name             VARCHAR(255),
    client_id             BIGINT,
    requested_stage       VARCHAR(100),
    current_stage         VARCHAR(100),
    current_department    VARCHAR(100),
    from_department       VARCHAR(100),
    requested_by_user_id  BIGINT,
    requested_by_name     VARCHAR(255),
    requested_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status                VARCHAR(50) DEFAULT 'PENDING',
    reviewed_by_user_id   BIGINT,
    reviewed_by_name      VARCHAR(255),
    reviewed_at           TIMESTAMP WITH TIME ZONE,
    rejection_reason      TEXT,
    value_amount          DECIMAL(15, 2),
    CONSTRAINT fk_approval_deal FOREIGN KEY (deal_id) REFERENCES deals(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_approval_status ON lead_closure_approvals(status);
CREATE INDEX IF NOT EXISTS idx_approval_deal_id ON lead_closure_approvals(deal_id);
CREATE INDEX IF NOT EXISTS idx_approval_requested_by ON lead_closure_approvals(requested_by_user_id);
CREATE INDEX IF NOT EXISTS idx_approval_requested_at ON lead_closure_approvals(requested_at DESC);
