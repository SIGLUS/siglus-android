INSERT INTO programs (code, name, description, active, templateConfigured,
  regimenTemplateConfigured, budgetingApplies, usesDar, push,isSupportEmergency) VALUES
('MMIA', 'MMIA', 'MMIA', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE),
('ESS_MEDS', 'ESSENTIAL MEDICINE', 'ESSENTIAL MEDICINE', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
('TB', 'TB', 'TB', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE),
('NUTRITION', 'NUTRITION', 'NUTRITION', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE),
('VIA', 'VIA Classica', 'VIA', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE),
('MALARIA', 'Malaria', 'Malaria', TRUE, TRUE , TRUE, FALSE, FALSE, FALSE, TRUE),
('PTV', 'PTV', 'PTV', TRUE, true , FALSE, FALSE, FALSE, FALSE, TRUE),
('TEST_KIT', 'Testes Rápidos Diag', 'Testes Rápidos Diag', TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, TRUE);


UPDATE programs SET parentId = (SELECT id FROM programs WHERE code = 'VIA')
WHERE code in ('TB', 'NUTRITION', 'ESS_MEDS');