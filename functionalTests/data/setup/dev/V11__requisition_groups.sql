INSERT INTO requisition_groups (code, name, supervisoryNodeId) VALUES
('RG1','Requistion Group MMIA', (SELECT id FROM supervisory_nodes WHERE code ='N1')),
('RG2','Requistion Group VIA', (SELECT id FROM supervisory_nodes WHERE code ='N2'));

INSERT INTO requisition_group_members (requisitionGroupId, facilityId) VALUES
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF1')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF2')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF3')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF4')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF5')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF6')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF7')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF8')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='HF9')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='F_VIA')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='F_STOCKCARD')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='F_PHYSICAL_INVENTORY')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='F_INITIAL_INVENTORY')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='F_KIT')),
((SELECT id FROM requisition_groups WHERE code ='RG2'), (SELECT id FROM facilities WHERE code ='F_CORE')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='HF2')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='HF3')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='HF5')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='HF6')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='HF7')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='HF9')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='F_MMIA')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='F_STOCKCARD')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='F_PHYSICAL_INVENTORY')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='F_INITIAL_INVENTORY')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='F_MMIA_MISMATCH')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='F_KIT')),
((SELECT id FROM requisition_groups WHERE code ='RG1'), (SELECT id FROM facilities WHERE code ='F_CORE'));

INSERT INTO requisition_group_program_schedules
(requisitionGroupId, programId, scheduleId, directDelivery ) VALUES
((SELECT id FROM requisition_groups WHERE code='RG1'), (SELECT id FROM programs WHERE code='MMIA'),
  (SELECT id FROM processing_schedules WHERE code='M'), TRUE),
((SELECT id FROM requisition_groups WHERE code='RG2'), (SELECT id FROM programs WHERE code='VIA'),
  (SELECT id FROM processing_schedules WHERE code='M'), TRUE);
