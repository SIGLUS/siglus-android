INSERT INTO processing_periods
(name, description, startDate, endDate, numberOfMonths, scheduleId, modifiedBy) VALUES
('Jan-21-2015', 'Jan2015', '2016-01-21', '2016-02-20 23:59:59', 1,
  (SELECT id FROM processing_schedules WHERE code = 'M'), (SELECT id FROM users LIMIT 1));

INSERT INTO facilities
(code, name, description, geographicZoneId, typeId, active, goLiveDate, enabled, sdp, virtualFacility)
VALUES
('HF1','Facility-1','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'1/21/2016',TRUE,TRUE,FALSE),
('HF2','Facility-2','',(SELECT id FROM geographic_zones WHERE code = 'MARRACUENE'),
  (SELECT id FROM facility_types WHERE code = 'CSRUR-II'),TRUE,'11/21/2015',TRUE,TRUE,FALSE);

INSERT INTO programs_supported (facilityId, programId, active, startDate) VALUES
((SELECT id FROM facilities WHERE code = 'HF1'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF1'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'1/21/2016'),
((SELECT id FROM facilities WHERE code = 'HF2'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'),TRUE,'11/21/2015'),
((SELECT id FROM facilities WHERE code = 'HF2'), (SELECT id FROM programs WHERE code = 'MMIA'),TRUE,'11/21/2015');

INSERT INTO requisition_group_members (requisitionGroupId, facilityId) VALUES
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='HF1')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='HF2')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF1')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF2'));

INSERT INTO users
(userName, password, facilityId, firstName, lastName, email, verified, active, restrictLogin, isMobileUser) VALUES
('facility1-user', 'vFR3ULknlislVs2ESzJvdXN330IYhUdA6FnraiiZWqJKmtJGELNqaLwC2iiQUHuUWcK6hPtZGkJmkRT8zXLI5212gieie',
  (SELECT id FROM facilities WHERE code = 'HF1'), 'Facility1', 'User', NULL,
  TRUE, TRUE, FALSE, TRUE),
('facility2-user', 'vFR3ULknlislVs2ESzJvdXN330IYhUdA6FnraiiZWqJKmtJGELNqaLwC2iiQUHuUWcK6hPtZGkJmkRT8zXLI5212gieie',
  (SELECT id FROM facilities WHERE code = 'HF2'), 'Facility2', 'User', NULL,
  TRUE, TRUE, FALSE, TRUE);

INSERT INTO role_assignments
(userId, roleId, programId, supervisoryNodeId) VALUES
((SELECT ID FROM USERS WHERE username = 'facility1-user'),
  (SELECT id FROM roles WHERE name = 'FacilityHead'), (SELECT id FROM programs WHERE code = 'MMIA'), NULL),
((SELECT ID FROM USERS WHERE username = 'facility1-user'),
  (SELECT id FROM roles WHERE name = 'FacilityHead'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'), NULL),
((SELECT ID FROM USERS WHERE username = 'facility2-user'),
  (SELECT id FROM roles WHERE name = 'FacilityHead'), (SELECT id FROM programs WHERE code = 'MMIA'), NULL),
((SELECT ID FROM USERS WHERE username = 'facility2-user'),
  (SELECT id FROM roles WHERE name = 'FacilityHead'), (SELECT id FROM programs WHERE code = 'ESS_MEDS'), NULL);
