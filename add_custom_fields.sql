-- Add JSON columns for custom fields
ALTER TABLE banks ADD COLUMN IF NOT EXISTS custom_fields JSON;
ALTER TABLE products ADD COLUMN IF NOT EXISTS custom_fields JSON;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS custom_fields JSON;

-- Add test custom fields for "Collect Payment" task type
INSERT INTO task_custom_fields 
(active, custom_task_type, field_key, field_label, field_type, required, sort_order)
VALUES 
(true, 'Collect Payment', 'amount', 'Amount', 'NUMBER', true, 1),
(true, 'Collect Payment', 'payment_mode', 'Payment Mode', 'DROPDOWN', true, 2),
(true, 'Collect Payment', 'receipt', 'Receipt Photo', 'PHOTO', false, 3),
(true, 'Collect Payment', 'notes', 'Collection Notes', 'TEXT', false, 4);

-- Also add fields for "Site Visit" task type
INSERT INTO task_custom_fields 
(active, custom_task_type, field_key, field_label, field_type, required, sort_order)
VALUES 
(true, 'Site Visit', 'site_name', 'Site Name', 'TEXT', true, 1),
(true, 'Site Visit', 'visit_purpose', 'Visit Purpose', 'DROPDOWN', true, 2),
(true, 'Site Visit', 'site_photo', 'Site Photo', 'PHOTO', false, 3),
(true, 'Site Visit', 'site_notes', 'Site Notes', 'TEXT', false, 4);

-- Verify the fields were added
SELECT * FROM task_custom_fields 
WHERE custom_task_type IN ('Collect Payment', 'Site Visit')
ORDER BY custom_task_type, sort_order;
