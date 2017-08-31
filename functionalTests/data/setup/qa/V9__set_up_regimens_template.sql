INSERT INTO regimen_categories (code, name, displayOrder) VALUES
('ADULTS', 'Adults', 1),
('PAEDIATRICS', 'Paediatrics', 2);

INSERT INTO regimens (programid, categoryid, code, name, active, displayorder) VALUES
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'ADULTS'), '001', 'AZT+3TC+NVP', TRUE, 1),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'ADULTS'), '002', 'TDF+3TC+EFV', TRUE, 2),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'ADULTS'), '003', 'AZT+3TC+EFV', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'ADULTS'), '004', 'd4T 30+3TC+NVP', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'ADULTS'), '005', 'd4T 30+3TC+EFV', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'ADULTS'), '006', 'AZT+3TC+LPV/r', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'ADULTS'), '007', 'TDF+3TC+LPV/r', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'ADULTS'), '008', 'ABC+3TC+LPV/r', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '009', 'd4T+3TC+NVP(3DFC Baby)', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '010', 'd4T+3TC+LPV/r(2DFC Baby + LPV/r)', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '011', 'd4T+3TC+ABC(2DFC Baby + ABC)', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '012', 'd4T+3TC+EFV(2DFC Baby + EFV)', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '013', 'AZT60+3TC+NVP(3DFC)', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '014', 'AZT60+3TC+EFV(2DFC + EFV', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '015', 'AZT60+3TC+ABC(2DFC + ABC)', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '016', 'AZT60+3TC+LPV/r(2DFC + LPV/r)', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '017', 'ABC+3TC+LPV/r', TRUE, 3),
((SELECT id FROM programs where code = 'MMIA'), (SELECT id FROM regimen_categories WHERE code = 'PAEDIATRICS'), '018', 'ABC+3TC+EFZ', TRUE, 3);

INSERT INTO program_regimen_columns(name, programId, label, visible, dataType) VALUES
('code', (SELECT id FROM programs where code = 'MMIA'), 'header.code',
  TRUE, 'regimen.reporting.dataType.text'),
('name', (SELECT id FROM programs where code = 'MMIA'), 'header.name',
  TRUE, 'regimen.reporting.dataType.text'),
('patientsOnTreatment', (SELECT id FROM programs where code = 'MMIA'), 'Number of patients on treatment',
  TRUE, 'regimen.reporting.dataType.numeric');