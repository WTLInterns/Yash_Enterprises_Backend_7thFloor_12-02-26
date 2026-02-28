// Sample data for StageMaster table
// Run these SQL queries to populate your database with department-based stages

// PPE Department Stages
INSERT INTO stage_master (department, stage_code, stage_name, stage_order, is_terminal) VALUES
('PPE', 'NEW_LEAD', 'New Lead', 1, false),
('PPE', 'PDO', 'PDO', 2, false),
('PPE', 'EVALUATION', 'Evaluation', 3, false),
('PPE', 'PPS', 'PPS', 4, false),
('PPE', 'REVIEW', 'Review', 5, false),
('PPE', 'DOP', 'DOP', 6, false),
('PPE', 'ACCOUNT', 'Account', 7, false);

// PPO Department Stages
INSERT INTO stage_master (department, stage_code, stage_name, stage_order, is_terminal) VALUES
('PPO', 'NEW_LEAD', 'New Lead', 1, false),
('PPO', 'DOC_COLLECT', 'Doc Collect', 2, false),
('PPO', 'DRAFT', 'Draft', 3, false),
('PPO', 'OTH', 'OTH', 4, false),
('PPO', 'FILING', 'Filing', 5, false),
('PPO', 'FOLLOWUP', 'Followup', 6, false),
('PPO', 'ACCOUNT', 'Account', 7, false);

// PSD Department Stages
INSERT INTO stage_master (department, stage_code, stage_name, stage_order, is_terminal) VALUES
('PSD', 'NEW_LEAD', 'New Lead', 1, false),
('PSD', 'EVALUATION', 'Evaluation', 2, false),
('PSD', 'BUYER_REGD', 'Buyer Registration', 3, false),
('PSD', 'EMD_REGD', 'EMD Registration', 4, false),
('PSD', 'SALE', 'Sale', 5, false),
('PSD', 'REMAINING_AMT', 'Remaining Amount', 6, false),
('PSD', 'ACCOUNT', 'Account', 7, false);

// HLC Department Stages
INSERT INTO stage_master (department, stage_code, stage_name, stage_order, is_terminal) VALUES
('HLC', 'NEW_LEAD', 'New Lead', 1, false),
('HLC', 'ELIGIBILITY', 'Eligibility', 2, false),
('HLC', 'DOCUMENTS', 'Documents', 3, false),
('HLC', 'PROCESSING', 'Processing', 4, false),
('HLC', 'LOAN_APP', 'Loan Application', 5, false),
('HLC', 'LOAN_SANCTION', 'Loan Sanction', 6, false),
('HLC', 'ACCOUNT', 'Account', 7, false);

// ROP Department Stages
INSERT INTO stage_master (department, stage_code, stage_name, stage_order, is_terminal) VALUES
('ROP', 'NEW_LEAD', 'New Lead', 1, false),
('ROP', 'LOD', 'LOD', 2, false),
('ROP', 'RRV', 'RRV', 3, false),
('ROP', 'QUOTATION', 'Quotation', 4, false),
('ROP', 'DRAFTING', 'Drafting', 5, false),
('ROP', 'REGISTRATION', 'Registration', 6, false),
('ROP', 'ACCOUNT', 'Account', 7, false);

// ACCOUNT Department Stages
INSERT INTO stage_master (department, stage_code, stage_name, stage_order, is_terminal) VALUES
('ACCOUNT', 'INVENTORY', 'Inventory', 1, false),
('ACCOUNT', 'MAKE_BILL', 'Make Bill', 2, false),
('ACCOUNT', 'BILL_SUBMIT', 'Bill Submit', 3, false),
('ACCOUNT', 'BILL_FOLLOWUP', 'Bill Followup', 4, false),
('ACCOUNT', 'BILL_PASS', 'Bill Pass', 5, false),
('ACCOUNT', 'CLOSE_WIN', 'Close Win', 6, true),
('ACCOUNT', 'CLOSE_LOST', 'Close Lost', 6, true);
