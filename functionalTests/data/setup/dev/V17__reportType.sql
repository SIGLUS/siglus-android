INSERT INTO reports_type (code, programid, name, description) VALUES
('MMIA', (SELECT id FROM programs WHERE code = 'MMIA'), 'MMIA', 'MMIA'),
('TEST_KIT ', (SELECT id FROM programs WHERE code = 'TEST_KIT'), 'RAPID TEST', 'RAPID TEST'),
('PTV', (SELECT id FROM programs WHERE code = 'PTV'), 'PTV', 'PTV'),
('VIA', (SELECT id FROM programs WHERE code = 'VIA'), 'Balance Requisition', 'Balance Requisition'),
('MALARIA', (SELECT id FROM programs WHERE code = 'MALARIA'), 'AL', 'AL');
