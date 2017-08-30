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
('DPM','DPM', 3, 0.5, TRUE),
('Central', 'Moçambique', 3, 0.5, TRUE);

INSERT INTO facilities
(code, name, description, geographicZoneId, typeId, active, goLiveDate, enabled, sdp, virtualFacility)
VALUES
('CENTRAL','Central','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'Central'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF1','Marracuene','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF2','Matalane','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF3','Machubo','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF4','Michafutane','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF5','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF6','Ricatla','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF7','Mumemo','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF8','Habel Jafar','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('HF9','Mali','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('DDM1','Marracuene','Deposito Provincial de Medicamentos de Maputo',
  (SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'DDM'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('DPM1','Maputo','Deposito DIstrital de Medicamentos de Marracuene',
  (SELECT id FROM geographic_zones WHERE code = 'MATOLA'),
  (SELECT id FROM facility_types WHERE code = 'DPM'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('F_MMIA_MISMATCH','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('F_MMIA','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('F_VIA','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('F_INITIAL_INVENTORY','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('F_PHYSICAL_INVENTORY','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('F_STOCKCARD','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('F_KIT','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE),
('F_CORE','Nhongonhane (Ed.Mondl.)','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'9/21/2013',TRUE,TRUE,FALSE);

INSERT INTO supervisory_nodes
(facilityId, name, code, parentId) VALUES
((SELECT id FROM facilities WHERE code = 'DPM1'), 'DPM supervisory node', 'N1', NULL),
((SELECT id FROM facilities WHERE code = 'DDM1'), 'DDM supervisory node', 'N2', NULL);

INSERT INTO programs_supported (facilityId, programId, active, startDate) VALUES
((SELECT id FROM facilities WHERE code = 'HF1'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF1'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF1'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF1'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF1'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'HF2'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF2'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF2'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF2'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF2'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'HF3'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF3'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF3'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF3'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF3'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'HF4'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF4'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF4'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF4'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF4'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'HF5'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF5'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF5'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF5'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF5'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'HF6'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF6'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF6'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF6'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF6'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'HF7'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF7'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF7'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF7'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF7'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'HF8'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF8'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF8'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF8'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF8'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'HF9'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF9'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF9'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF9'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF9'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'F_VIA'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_VIA'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_VIA'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_VIA'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_VIA'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'F_STOCKCARD'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_STOCKCARD'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_STOCKCARD'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_STOCKCARD'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_STOCKCARD'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'F_KIT'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'9/21/2013'),
((SELECT id FROM facilities WHERE code = 'F_KIT'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'9/21/2013'),
((SELECT id FROM facilities WHERE code = 'F_KIT'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'9/21/2013'),
((SELECT id FROM facilities WHERE code = 'F_KIT'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_KIT'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'F_PHYSICAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_PHYSICAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_PHYSICAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_PHYSICAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_PHYSICAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'F_INITIAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_INITIAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_INITIAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_INITIAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_INITIAL_INVENTORY'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'F_MMIA_MISMATCH'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_MMIA_MISMATCH'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_MMIA_MISMATCH'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_MMIA_MISMATCH'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_MMIA_MISMATCH'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'F_MMIA'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_MMIA'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_MMIA'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_MMIA'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_MMIA'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016'),

((SELECT id FROM facilities WHERE code = 'F_CORE'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_CORE'), (SELECT id FROM programs WHERE code = 'VIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_CORE'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_CORE'), (SELECT id FROM programs WHERE code = 'TB'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'F_CORE'), (SELECT id FROM programs WHERE code = 'NUTRITION'),TRUE,'1/21/2016');
