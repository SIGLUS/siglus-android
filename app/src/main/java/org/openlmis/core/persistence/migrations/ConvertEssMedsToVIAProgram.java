package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class ConvertEssMedsToVIAProgram extends Migration {
    @Override
    public void up() {
        execSQL("UPDATE rnr_forms SET program_id = (SELECT id FROM programs WHERE programCode = 'VIA') " +
                "WHERE program_id = (SELECT id FROM programs WHERE programCode = 'ESS_MEDS')");
    }
}
