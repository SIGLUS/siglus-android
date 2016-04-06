package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddIsEmergencyColumnToProgram extends Migration {

    @Override
    public void up() {
        // The default value should be set as 0 instead of false. Updated it in the later migration
        execSQL("ALTER TABLE 'programs' ADD COLUMN isEmergency BOOLEAN DEFAULT 0");

        execSQL("UPDATE programs SET isEmergency = 1 WHERE programCode != 'TARV' AND parentCode IS NOT NULL");
    }
}
