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
(17, null, (select id from programs where code = 'MMIA'), FALSE, 'U', 18, 'Requested Quantity'),
(18, null, (select id from programs where code = 'MMIA'), FALSE, 'U', 19, 'Requested Quantity Explanation'),
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