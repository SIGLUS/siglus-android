package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddIsEmergencyColumnToProgram extends Migration {

  @Override
  public void up() {
    // The default value should be set as 0 instead of false. Updated it in the later migration
    execSQL("ALTER TABLE 'programs' ADD COLUMN isSupportEmergency BOOLEAN DEFAULT 0");

    execSQL(
        "UPDATE programs SET isSupportEmergency = 1 WHERE programCode IN ('VIA','TEST_KIT','TB','MALARIA','PME','NUTRITION','ESS_MEDS')");
  }
}
