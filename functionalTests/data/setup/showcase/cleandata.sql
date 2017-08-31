DELETE FROM moz_app_info;

DELETE FROM stock_card_entry_key_values;
DELETE FROM stock_card_entries;
DELETE FROM stock_movement_line_items;
DELETE FROM stock_cards;

DELETE FROM requisition_signatures;
DELETE FROM signatures;
DELETE FROM requisition_periods;

DELETE FROM patient_quantification_line_items;
DELETE FROM requisition_line_item_losses_adjustments;

DELETE FROM comments;
DELETE FROM regimen_line_items;

DELETE FROM requisition_line_items;
DELETE FROM requisition_status_changes;
DELETE FROM requisitions;

DELETE FROM requisition_group_members;

DELETE FROM processing_periods;

DELETE FROM facility_approved_products;
DELETE FROM program_products;
DELETE FROM kit_products_relation;
DELETE FROM products;

DELETE FROM role_assignments WHERE userid != 1;
DELETE FROM user_password_reset_tokens;
DELETE FROM users WHERE username != 'Admin123';

DELETE FROM programs_supported;
DELETE FROM facilities WHERE code != 'DDM1' AND code != 'DPM1';

DELETE FROM email_attachments_relation;
DELETE FROM email_attachments;
DELETE FROM email_notifications;
DELETE FROM sync_up_hashes;
