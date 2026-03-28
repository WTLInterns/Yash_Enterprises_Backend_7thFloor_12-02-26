CREATE TABLE IF NOT EXISTS employee_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(500),
    content_type VARCHAR(100),
    file_size BIGINT,
    uploaded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    employee_id BIGINT NOT NULL,
    CONSTRAINT fk_emp_doc_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);
