INSERT INTO supplemental_programs (code, name, description, active)
VALUES ('RAPID_TEST', 'Rapid Test', 'Rapid test', TRUE);

INSERT INTO program_data_columns (code, supplementalProgramId) VALUES
('CONSUME_HIVDETERMINE', (SELECT id FROM supplemental_programs WHERE code = 'RAPID_TEST'));

INSERT INTO program_data_columns (code, supplementalProgramId) VALUES
('POSITIVE_HIVDETERMINE', (SELECT id FROM supplemental_programs WHERE code = 'RAPID_TEST'));

INSERT INTO program_data_columns (code, supplementalProgramId) VALUES
('CONSUME_HIVUNIGOLD', (SELECT id FROM supplemental_programs WHERE code = 'RAPID_TEST'));

INSERT INTO program_data_columns (code, supplementalProgramId) VALUES
('POSITIVE_HIVUNIGOLD', (SELECT id FROM supplemental_programs WHERE code = 'RAPID_TEST'));

INSERT INTO program_data_columns (code, supplementalProgramId) VALUES
('CONSUME_SYPHILLIS', (SELECT id FROM supplemental_programs WHERE code = 'RAPID_TEST'));

INSERT INTO program_data_columns (code, supplementalProgramId) VALUES
('POSITIVE_SYPHILLIS', (SELECT id FROM supplemental_programs WHERE code = 'RAPID_TEST'));

INSERT INTO program_data_columns (code, supplementalProgramId) VALUES
('CONSUME_MALARIA', (SELECT id FROM supplemental_programs WHERE code = 'RAPID_TEST'));

INSERT INTO program_data_columns (code, supplementalProgramId) VALUES
('POSITIVE_MALARIA', (SELECT id FROM supplemental_programs WHERE code = 'RAPID_TEST'));