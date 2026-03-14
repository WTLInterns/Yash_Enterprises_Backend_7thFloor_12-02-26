-- Add index to optimize client + employee task filtering
CREATE INDEX idx_tasks_client_employee ON tasks(client_id, assigned_to_employee_id);
