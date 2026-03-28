CREATE TABLE IF NOT EXISTS bank_contact_persons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_id BIGINT NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    position VARCHAR(255),
    CONSTRAINT fk_bcp_bank FOREIGN KEY (bank_id) REFERENCES banks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bank_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(1000),
    content_type VARCHAR(255),
    file_size BIGINT,
    uploaded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bdoc_bank FOREIGN KEY (bank_id) REFERENCES banks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bank_email_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_id BIGINT NOT NULL,
    to_email VARCHAR(255),
    cc_email VARCHAR(255),
    subject VARCHAR(500),
    body LONGTEXT,
    attachment_name VARCHAR(255),
    attachment_path VARCHAR(1000),
    sent_by BIGINT,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'SENT'
);
