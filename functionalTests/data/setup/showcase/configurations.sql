INSERT INTO programs (code, name, description, active, templateConfigured,
  regimenTemplateConfigured, budgetingApplies, usesDar, push) VALUES
('MMIA', 'MMIA', 'MMIA', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE),
('ESS_MEDS', 'VIA ESSENTIAL', 'VIA ESSENTIAL', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE);

INSERT INTO geographic_levels (code, name, levelNumber) VALUES
('national', 'National', 1),
('province', 'Province', 2),
('district', 'District', 3);

INSERT INTO geographic_zones
(code, name, levelId, parentId) VALUES
('MOZ', 'Mozambique', (select id from geographic_levels where code = 'national'), null);

INSERT INTO geographic_zones
(code, name, levelId, parentId) VALUES
('MAPUTO_PROVINCIA', 'Maputo Província', (select id from geographic_levels where code = 'province'), (select id from geographic_zones where code = 'MOZ'));

INSERT INTO geographic_zones
(code, name, levelId, parentId) VALUES
('MARRACUENE', 'Marracuene', (select id from geographic_levels where code = 'district'), (select id from geographic_zones where code = 'MAPUTO_PROVINCIA'));

INSERT INTO geographic_zones
(code, name, levelId, parentId) VALUES
('MATOLA', 'Matola', (select id from geographic_levels where code = 'district'), (select id from geographic_zones where code = 'MAPUTO_PROVINCIA'));


INSERT INTO facility_types (code, name, nominalMaxMonth, nominalEop, active) VALUES
('CSRUR-I','CSRUR - I', 3, 0.5, TRUE),
('CSRUR-II','CSRUR - II', 3, 0.5, TRUE),
('DDM','DDM', 3, 0.5, TRUE),
('DPM','DPM', 3, 0.5, TRUE);

INSERT INTO dosage_units
(code, displayOrder) VALUES
('mg',1),
('mL',2),
('g',3),
('%',4),
('UI',5),
('mcg',6),
('mm',7),
('cm',8),
('unidade',9),
('other',10);

INSERT INTO product_forms
(code, displayOrder) VALUES
('Comprimidos',1),
('Gotas Orais',2),
('Injectável',3),
('Cápsulas',4),
('Suspensão',5),
('Gel',6),
('Supositório',7),
('Xarope',8),
('Microenema',9),
('Pó',10),
('Solução',11),
('Emulsão',12),
('Enema',13),
('Créme',14),
('Pomada',15),
('Créme vaginal',16),
('Óvulo vaginal',17),
('Comprimidos vaginais',18),
('Ciclo',19),
('Sistemas transdérmicos',20),
('Implante',21),
('DIU',22),
('Preservativo',23),
('Spray nasal',24),
('Granulado',25),
('Solução Aquosa',26),
('Aerosol',27),
('Solução para Nebulização',28),
('Cubos',29),
('Comprimidos de Libertação Prolongada',30),
('Cápsulas de Libertação Prolongada',31),
('Injectável Ampola',32),
('Injectável Frasco',33),
('KIT',34),
('Tratamento',35),
('Saquetas',36),
('Embalagem',37),
('Solução Oral',38),
('Bolsa compart injectavel',39),
('Ampola Bebível',40),
('Elixir',41),
('Loção Aquosa',42),
('Linimento',43),
('Líquido',44),
('Loção',45),
('Pasta',46),
('Loção Capilar',47),
('Frasco',48),
('Solução Ungueal',49),
('Solução alcoólica',50),
('Champo',51),
('Cristais',52),
('Lápis',53),
('Gotas auriculares',54),
('Gotas nasais',55),
('Soluto',56),
('Gel oral',57),
('Colírio',58),
('Pomada oftálmica',59),
('Solução Oftálmica',60),
('Papel',61),
('Gás',62),
('Spray',63),
('Solução - Frasco',64),
('Sabonete',65),
('Testes',66),
('Pó para Suspensão',67),
('Rolo',68),
('Compressa',69),
('Seringas',70),
('Rede',71),
('Sem',72),
('Esponjas', 73),
('Gotas oftálmicas', 74),
('Saqueta', 75),
('Shampô', 76),
('Tests', 77);

INSERT INTO product_categories
(code, name , displayOrder) VALUES
('1', 'Antibiotics', 1),
('2', 'Analgesics', 2),
('3', 'Inhalers', 3),
('4', 'Injections', 4),
('5', 'Drops', 5),
('6', 'Buccal', 6),
('7', 'Anabolics', 7),
('8', 'Vitamin Supplements', 8),
('9', 'Anesthetics', 9),
('10', 'Sedative', 10),
('11', 'Other', 11);

ALTER TABLE products ALTER COLUMN strength TYPE varchar(50);

UPDATE master_rnr_columns SET name = 'totalLossesAndAdjustments', formula = '' where name = 'lossesAndAdjustments';

INSERT INTO program_rnr_columns
(masterColumnId, rnrOptionId, programId, visible, source, position, label) VALUES
(1, null, (select id from programs where code = 'MMIA'),  TRUE,'U', 1,  'Skip'),
(2, null, (select id from programs where code = 'MMIA'),  TRUE, 'R', 2,  'Product Code'),
(3, null, (select id from programs where code = 'MMIA'),  FALSE, 'R', 3,  'Product'),
(4, null, (select id from programs where code = 'MMIA'),  FALSE, 'R', 4,  'Unit/Unit of Issue'),
(5, null, (select id from programs where code = 'MMIA'),  TRUE, 'U', 5,  'Beginning Balance'),
(6, null, (select id from programs where code = 'MMIA'),  TRUE, 'U', 6,  'Total Received Quantity'),
(7, null, (select id from programs where code = 'MMIA'),  FALSE, 'C', 7,  'Total'),
(8, null, (select id from programs where code = 'MMIA'),  TRUE, 'U', 8,  'Total Consumed Quantity'),
(9, null, (select id from programs where code = 'MMIA'),  TRUE, 'U', 9,  'Total Losses / Adjustments'),
(10, null, (select id from programs where code = 'MMIA'), TRUE, 'U', 10,  'Stock on Hand'),
(11, 1   , (select id from programs where code = 'MMIA'), FALSE, 'U', 11, 'New Patients'),
(12, null, (select id from programs where code = 'MMIA'), FALSE, 'U', 12, 'Total Stockout Days'),
(13, null, (select id from programs where code = 'MMIA'), FALSE, 'C', 13, 'Monthly Normalized Consumption'),
(25, null, (select id from programs where code = 'MMIA'), FALSE, 'C', 14, 'Period Normalized Consumption'),
(14, null, (select id from programs where code = 'MMIA'), FALSE, 'C', 15, 'Average Monthly Consumption(AMC)'),
(15, null, (select id from programs where code = 'MMIA'), FALSE, 'C', 16, 'Maximum Stock Quantity'),
(16, null, (select id from programs where code = 'MMIA'), FALSE, 'C', 17, 'Calculated Order Quantity'),
(17, null, (select id from programs where code = 'MMIA'), TRUE, 'U', 18, 'Requested Quantity'),
(18, null, (select id from programs where code = 'MMIA'), TRUE, 'U', 19, 'Requested Quantity Explanation'),
(19, null, (select id from programs where code = 'MMIA'), FALSE, 'U', 20, 'Approved Quantity'),
(20, null, (select id from programs where code = 'MMIA'), FALSE, 'C', 21, 'Packs to Ship'),
(21, null, (select id from programs where code = 'MMIA'), FALSE, 'R', 22, 'Price per Pack'),
(22, null, (select id from programs where code = 'MMIA'), FALSE, 'C', 23, 'Total Cost'),
(23, null, (select id from programs where code = 'MMIA'), TRUE, 'U', 24, 'Expiration Date(MM/YYYY)'),
(24, null, (select id from programs where code = 'MMIA'), FALSE, 'U', 25, 'Remarks');
UPDATE programs SET templateConfigured = TRUE WHERE id = (SELECT id FROM programs WHERE code = 'MMIA');

INSERT INTO program_rnr_columns
(masterColumnId, rnrOptionId, programId, visible, source, position, label) VALUES
(1, null, (select id from programs where code = 'ESS_MEDS'),  TRUE,'U', 1,  'Skip'),
(2, null, (select id from programs where code = 'ESS_MEDS'),  TRUE, 'R', 2,  'Product Code'),
(3, null, (select id from programs where code = 'ESS_MEDS'),  FALSE, 'R', 3,  'Product'),
(4, null, (select id from programs where code = 'ESS_MEDS'),  FALSE, 'R', 4,  'Unit/Unit of Issue'),
(5, null, (select id from programs where code = 'ESS_MEDS'),  TRUE, 'U', 5,  'Beginning Balance'),
(6, null, (select id from programs where code = 'ESS_MEDS'),  TRUE, 'U', 6,  'Total Received Quantity'),
(7, null, (select id from programs where code = 'ESS_MEDS'),  FALSE, 'C', 7,  'Total'),
(8, null, (select id from programs where code = 'ESS_MEDS'),  TRUE, 'U', 8,  'Total Consumed Quantity'),
(9, null, (select id from programs where code = 'ESS_MEDS'),  TRUE, 'U', 9,  'Total Losses / Adjustments'),
(10, null, (select id from programs where code = 'ESS_MEDS'), TRUE, 'U', 10,  'Stock on Hand'),
(11, 1   , (select id from programs where code = 'ESS_MEDS'), FALSE, 'U', 11, 'New Patients'),
(12, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'U', 12, 'Total Stockout Days'),
(13, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'C', 13, 'Monthly Normalized Consumption'),
(25, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'C', 14, 'Period Normalized Consumption'),
(14, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'C', 15, 'Average Monthly Consumption(AMC)'),
(15, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'C', 16, 'Maximum Stock Quantity'),
(16, null, (select id from programs where code = 'ESS_MEDS'), TRUE, 'U', 17, 'Calculated Order Quantity'),
(17, null, (select id from programs where code = 'ESS_MEDS'), TRUE, 'U', 18, 'Requested Quantity'),
(18, null, (select id from programs where code = 'ESS_MEDS'), TRUE, 'U', 19, 'Requested Quantity Explanation'),
(19, null, (select id from programs where code = 'ESS_MEDS'), TRUE, 'U', 20, 'Approved Quantity'),
(20, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'C', 21, 'Packs to Ship'),
(21, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'R', 22, 'Price per Pack'),
(22, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'C', 23, 'Total Cost'),
(23, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'U', 24, 'Expiration Date(MM/YYYY)'),
(24, null, (select id from programs where code = 'ESS_MEDS'), FALSE, 'U', 25, 'Remarks');
UPDATE programs SET templateConfigured = TRUE WHERE id = (SELECT id FROM programs WHERE code = 'ESS_MEDS');

INSERT INTO processing_schedules (code, name, description) VALUES ('M', 'Monthly', 'Month');

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

INSERT INTO losses_adjustments_types (name, description, additive) VALUES
('DISTRICT_DDM', 'District(DDM)', TRUE),
('PROVINCE_DPM', 'Province (DPM)', TRUE),
('PUB_PHARMACY', 'Public pharmacy', FALSE),
('MATERNITY', 'Maternity', FALSE),
('GENERAL_WARD', 'General Ward', FALSE),
('ACC_EMERGENCY', 'Accident & Emergency', FALSE),
('MOBILE_UNIT', 'Mobile unit', FALSE),
('LABORATORY', 'Laboratory', FALSE),
('UATS', 'UATS', FALSE),
('PNCTL', 'PNCTL', FALSE),
('PAV', 'PAV', FALSE),
('DENTAL_WARD', 'Dental ward', FALSE),
('UNPACK_KIT', 'Unpack kit', FALSE),
('EXPIRED_RETURN_TO_SUPPLIER', 'Drugs in quarantine have expired returned to Supplier', FALSE),
('DAMAGED', 'Damaged on arrival', FALSE),
('LOANS_DEPOSIT', 'Loans made from a health facility deposit', FALSE),
('PROD_DEFECTIVE', 'Product defective moved to quarantine"', FALSE),
('CUSTOMER_RETURN', 'Returns from Customers (HF and dependent wards)', TRUE),
('EXPIRED_RETURN_FROM_CUSTOMER', 'Returns of expired drugs (HF and dependent wards)', TRUE),
('DONATION', 'Donations to Deposit', TRUE),
('LOANS_RECEIVED', 'Loans received at the health facility deposit', TRUE),
('RETURN_FROM_QUARANTINE', '"Returns from Quarantine in the case of quarantined product being fit for use"', TRUE),
('INVENTORY', 'Inventory', TRUE),
('INVENTORY_NEGATIVE', 'Inventory correction in case of over stock on Stock card', FALSE),
('INVENTORY_POSITIVE', 'Inventory correction in case of under stock on Stock card', TRUE),
('DEFAULT_ISSUE', 'Issued', FALSE),
('DEFAULT_RECEIVE', 'Received', TRUE),
('DEFAULT_NEGATIVE_ADJUSTMENT', 'Negative adjustment', FALSE),
('DEFAULT_POSITIVE_ADJUSTMENT', 'Positive adjustment', TRUE);

INSERT INTO facilities
(code, name, description, geographicZoneId, typeId, active, goLiveDate, enabled, sdp, virtualFacility)
VALUES
('DDM1','DDM','DDM', (SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'DDM'),TRUE,'11/21/2015',TRUE,TRUE,FALSE),
('DPM1','DPM','DPM', (SELECT id FROM geographic_zones WHERE code = 'MATOLA'),
  (SELECT id FROM facility_types WHERE code = 'DPM'),TRUE,'11/21/2015',TRUE,TRUE,FALSE);

INSERT INTO supervisory_nodes
(facilityId, name, code, parentId) VALUES
((SELECT id FROM facilities WHERE code = 'DDM1'), 'DDM supervisory node', 'N1', NULL),
((SELECT id FROM facilities WHERE code = 'DPM1'), 'DPM supervisory node', 'N2', NULL);

INSERT INTO requisition_groups (code, name, supervisoryNodeId) VALUES
('RG1','Requistion Group VIA', (SELECT id FROM supervisory_nodes WHERE code ='N1')),
('RG2','Requistion Group MMIA', (SELECT id FROM supervisory_nodes WHERE code ='N2'));

INSERT INTO requisition_group_program_schedules
(requisitionGroupId, programId, scheduleId, directDelivery ) VALUES
((SELECT id FROM requisition_groups WHERE code='RG1'), (SELECT id FROM programs WHERE code='ESS_MEDS'),
  (SELECT id FROM processing_schedules WHERE code='M'), TRUE),
((SELECT id FROM requisition_groups WHERE code='RG2'), (SELECT id FROM programs WHERE code='MMIA'),
  (SELECT id FROM processing_schedules WHERE code='M'), TRUE);

INSERT INTO roles
(name, description) VALUES
('FacilityHead', ''),
('ReportViewer', ''),
('Supervisor', '');

INSERT INTO role_assignments
(userId, roleId, programId, supervisoryNodeId) VALUES
((SELECT ID FROM USERS WHERE username = 'Admin123'),
  (SELECT id FROM roles WHERE name = 'Supervisor'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),
  (SELECT id FROM supervisory_nodes WHERE code = 'N1')),
((SELECT ID FROM USERS WHERE username = 'Admin123'),
  (SELECT id FROM roles WHERE name = 'Supervisor'), (SELECT id FROM programs WHERE code = 'MMIA'),
  (SELECT id FROM supervisory_nodes WHERE code = 'N2')),
((SELECT ID FROM USERS WHERE username = 'Admin123'),
  (SELECT id FROM roles WHERE name = 'ReportViewer'), NULL, NULL);

UPDATE users SET email = 'openlmis.test.dpm@gmail.com' WHERE userName = 'Admin123';


