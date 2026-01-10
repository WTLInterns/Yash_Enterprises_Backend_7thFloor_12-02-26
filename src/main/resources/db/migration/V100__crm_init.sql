-- CRM initial schema
CREATE TABLE IF NOT EXISTS deals (
  id CHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  value_amount DECIMAL(15,2),
  closing_date DATE,
  branch_name VARCHAR(255),
  related_bank_name VARCHAR(255),
  description TEXT,
  required_amount DECIMAL(15,2),
  outstanding_amount DECIMAL(15,2),
  stage VARCHAR(64),
  owner_id CHAR(36),
  created_by CHAR(36),
  created_at DATETIME(6),
  modified_by CHAR(36),
  modified_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS deal_stage_history (
  id CHAR(36) PRIMARY KEY,
  deal_id CHAR(36) NOT NULL,
  previous_stage VARCHAR(64),
  new_stage VARCHAR(64),
  changed_by CHAR(36),
  changed_at DATETIME(6),
  note VARCHAR(1000)
);

ALTER TABLE deal_stage_history
  ADD CONSTRAINT fk_deal_stage_history_deal
  FOREIGN KEY (deal_id) REFERENCES deals(id)
  ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS activities (
  id CHAR(36) PRIMARY KEY,
  deal_id CHAR(36) NOT NULL,
  type VARCHAR(16) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  owner_id CHAR(36),
  due_date DATETIME(6),
  start_date DATETIME(6),
  end_date DATETIME(6),
  status VARCHAR(16),
  priority VARCHAR(16),
  repeat_rule VARCHAR(255),
  reminder DATETIME(6),
  created_by CHAR(36),
  created_at DATETIME(6),
  modified_by CHAR(36),
  modified_at DATETIME(6)
);

ALTER TABLE activities
  ADD CONSTRAINT fk_activities_deal
  FOREIGN KEY (deal_id) REFERENCES deals(id)
  ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS notes (
  id CHAR(36) PRIMARY KEY,
  deal_id CHAR(36) NOT NULL,
  title VARCHAR(255),
  body TEXT,
  created_by CHAR(36),
  created_at DATETIME(6)
);

ALTER TABLE notes
  ADD CONSTRAINT fk_notes_deal
  FOREIGN KEY (deal_id) REFERENCES deals(id)
  ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS files (
  id CHAR(36) PRIMARY KEY,
  deal_id CHAR(36),
  uploaded_by CHAR(36),
  file_name VARCHAR(512),
  file_size BIGINT,
  content_type VARCHAR(255),
  storage_path VARCHAR(1000),
  created_at DATETIME(6)
);

ALTER TABLE files
  ADD CONSTRAINT fk_files_deal
  FOREIGN KEY (deal_id) REFERENCES deals(id)
  ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS product_lines (
  id CHAR(36) PRIMARY KEY,
  deal_id CHAR(36) NOT NULL,
  product_name VARCHAR(255),
  list_price DECIMAL(15,2),
  quantity DECIMAL(15,2),
  discount DECIMAL(15,2),
  total DECIMAL(15,2),
  created_by CHAR(36),
  created_at DATETIME(6),
  modified_by CHAR(36),
  modified_at DATETIME(6)
);

ALTER TABLE product_lines
  ADD CONSTRAINT fk_product_lines_deal
  FOREIGN KEY (deal_id) REFERENCES deals(id)
  ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS email_records (
  id CHAR(36) PRIMARY KEY,
  deal_id CHAR(36) NOT NULL,
  to_address VARCHAR(512),
  cc_address VARCHAR(512),
  subject VARCHAR(512),
  body TEXT,
  sent_by CHAR(36),
  sent_at DATETIME(6),
  status VARCHAR(32)
);

ALTER TABLE email_records
  ADD CONSTRAINT fk_email_records_deal
  FOREIGN KEY (deal_id) REFERENCES deals(id)
  ON DELETE CASCADE;
