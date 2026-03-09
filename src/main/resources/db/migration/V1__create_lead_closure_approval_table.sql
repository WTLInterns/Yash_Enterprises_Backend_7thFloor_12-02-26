-- Create lead_closure_approval table
CREATE TABLE lead_closure_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deal_id BIGINT NOT NULL,
    requested_by BIGINT NOT NULL,
    requested_stage VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_deal_id (deal_id),
    INDEX idx_requested_by (requested_by),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (deal_id) REFERENCES deals(id),
    FOREIGN KEY (requested_by) REFERENCES employees(id),
    FOREIGN KEY (approved_by) REFERENCES employees(id)
);

-- Add check constraint for status
ALTER TABLE lead_closure_approval 
ADD CONSTRAINT chk_approval_status 
CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'));

-- Add check constraint for requested stage
ALTER TABLE lead_closure_approval 
ADD CONSTRAINT chk_requested_stage 
CHECK (requested_stage IN ('CLOSE_WON', 'CLOSE_LOST'));
