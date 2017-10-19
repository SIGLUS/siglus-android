package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePTVProgramTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `ptv_program` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`startPeriod` VARCHAR NOT NULL, "
                + "`endPeriod` VARCHAR NOT NULL, "
                + "`madeBy` VARCHAR NOT NULL, "
                + "`verifiedBy` VARCHAR NOT NULL);");
    }
}
