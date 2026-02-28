-- Create stage_master table for CRM deal stages
CREATE TABLE IF NOT EXISTS `stage_master` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `department` varchar(100) NOT NULL COMMENT 'Deal department: PPO, ROP, LEGAL, RECOVERY, etc.',
  `stage_code` varchar(50) NOT NULL COMMENT 'Stage code: NEW_LEAD, DOC_COLLECT, etc.',
  `stage_name` varchar(100) NOT NULL COMMENT 'Display name for UI',
  `stage_order` int DEFAULT 0 COMMENT 'Order for sorting stages',
  `is_terminal` bit(1) DEFAULT b'0' COMMENT 'Final stage (Win/Lost)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_department_stage` (`department`, `stage_code`),
  KEY `idx_department` (`department`),
  KEY `idx_department_order` (`department`, `stage_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert CRM deal stages (these are NOT address states)
INSERT INTO `stage_master` (`department`, `stage_code`, `stage_name`, `stage_order`, `is_terminal`) VALUES
-- PPO Department
('PPO', 'NEW_LEAD', 'New Lead', 1, b'0'),
('PPO', 'DOC_COLLECT', 'Document Collection', 2, b'0'),
('PPO', 'PDO', 'PDO Processing', 3, b'0'),
('PPO', 'DISBURSE', 'Disbursement', 4, b'0'),
('PPO', 'CLOSED_WON', 'Closed Won', 5, b'1'),
('PPO', 'CLOSED_LOST', 'Closed Lost', 6, b'1'),

-- ROP Department  
('ROP', 'NEW_LEAD', 'New Lead', 1, b'0'),
('ROP', 'DOC_COLLECT', 'Document Collection', 2, b'0'),
('ROP', 'VERIFICATION', 'Verification', 3, b'0'),
('ROP', 'APPROVAL', 'Approval', 4, b'0'),
('ROP', 'DISBURSE', 'Disbursement', 5, b'0'),
('ROP', 'CLOSED_WON', 'Closed Won', 6, b'1'),
('ROP', 'CLOSED_LOST', 'Closed Lost', 7, b'1'),

-- LEGAL Department
('LEGAL', 'NEW_CASE', 'New Case', 1, b'0'),
('LEGAL', 'DOCUMENTATION', 'Documentation', 2, b'0'),
('LEGAL', 'FILING', 'Filing', 3, b'0'),
('LEGAL', 'HEARING', 'Hearing', 4, b'0'),
('LEGAL', 'SETTLEMENT', 'Settlement', 5, b'0'),
('LEGAL', 'CLOSED_WON', 'Closed Won', 6, b'1'),
('LEGAL', 'CLOSED_LOST', 'Closed Lost', 7, b'1'),

-- RECOVERY Department
('RECOVERY', 'NEW_CASE', 'New Case', 1, b'0'),
('RECOVERY', 'NOTICE', 'Legal Notice', 2, b'0'),
('RECOVERY', 'FOLLOWUP', 'Follow Up', 3, b'0'),
('RECOVERY', 'NEGOTIATION', 'Negotiation', 4, b'0'),
('RECOVERY', 'RECOVERY', 'Recovery', 5, b'0'),
('RECOVERY', 'CLOSED_WON', 'Closed Won', 6, b'1'),
('RECOVERY', 'CLOSED_LOST', 'Closed Lost', 7, b'1'),

-- HLC Department
('HLC', 'NEW_LEAD', 'New Lead', 1, b'0'),
('HLC', 'EVALUATION', 'Evaluation', 2, b'0'),
('HLC', 'PROCESSING', 'Processing', 3, b'0'),
('HLC', 'APPROVAL', 'Approval', 4, b'0'),
('HLC', 'DISBURSE', 'Disbursement', 5, b'0'),
('HLC', 'CLOSED_WON', 'Closed Won', 6, b'1'),
('HLC', 'CLOSED_LOST', 'Closed Lost', 7, b'1'),

-- PPE Department
('PPE', 'NEW_LEAD', 'New Lead', 1, b'0'),
('PPE', 'DOCUMENTATION', 'Documentation', 2, b'0'),
('PPE', 'PROCESSING', 'Processing', 3, b'0'),
('PPE', 'APPROVAL', 'Approval', 4, b'0'),
('PPE', 'DISBURSE', 'Disbursement', 5, b'0'),
('PPE', 'CLOSED_WON', 'Closed Won', 6, b'1'),
('PPE', 'CLOSED_LOST', 'Closed Lost', 7, b'1'),

-- ACCOUNT Department
('ACCOUNT', 'NEW_ACCOUNT', 'New Account', 1, b'0'),
('ACCOUNT', 'VERIFICATION', 'Verification', 2, b'0'),
('ACCOUNT', 'SETUP', 'Account Setup', 3, b'0'),
('ACCOUNT', 'ACTIVE', 'Active', 4, b'0'),
('ACCOUNT', 'CLOSED', 'Closed', 5, b'1');

-- Verify the data
SELECT 
    department,
    GROUP_CONCAT(stage_code ORDER BY stage_order) as stages,
    COUNT(*) as stage_count
FROM stage_master 
GROUP BY department 
ORDER BY department;
