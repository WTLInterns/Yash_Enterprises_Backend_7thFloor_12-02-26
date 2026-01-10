-- V105__activities_module.sql
-- MySQL-compatible activities module

-- Base activities table (if not already created earlier)
CREATE TABLE IF NOT EXISTS activities (
  id CHAR(36) PRIMARY KEY,
  deal_id CHAR(36) NOT NULL,
  type VARCHAR(50) NOT NULL,
  subject VARCHAR(255),
  description TEXT,
  status VARCHAR(50),
  created_by CHAR(36),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
);

-- Task-specific fields
CREATE TABLE IF NOT EXISTS task_activities (
  id CHAR(36) NOT NULL,
  task_name VARCHAR(255),
  due_date DATE,
  is_repeat BOOLEAN,
  is_reminder BOOLEAN,
  task_status VARCHAR(50),
  priority VARCHAR(50),
  expense_amount DECIMAL(15,2),
  expense_description VARCHAR(500),
  PRIMARY KEY (id),
  CONSTRAINT fk_task_activity
    FOREIGN KEY (id) REFERENCES activities(id)
    ON DELETE CASCADE
);

-- Event-specific fields
CREATE TABLE IF NOT EXISTS event_activities (
  id CHAR(36) NOT NULL,
  start_datetime DATETIME,
  end_datetime DATETIME,
  location VARCHAR(255),
  PRIMARY KEY (id),
  CONSTRAINT fk_event_activity
    FOREIGN KEY (id) REFERENCES activities(id)
    ON DELETE CASCADE
);

-- Call-specific fields
CREATE TABLE IF NOT EXISTS call_activities (
  id CHAR(36) NOT NULL,
  call_datetime DATETIME,
  call_duration_minutes INT,
  call_result VARCHAR(255),
  PRIMARY KEY (id),
  CONSTRAINT fk_call_activity
    FOREIGN KEY (id) REFERENCES activities(id)
    ON DELETE CASCADE
);
