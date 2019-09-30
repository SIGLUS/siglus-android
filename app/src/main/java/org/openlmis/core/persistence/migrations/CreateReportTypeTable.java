package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateReportTypeTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `reports_type` "
                + "(`code` VARCHAR, "
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`name` VARCHAR, "
                + "`description` VARCHAR, "
                + "`active` BOOLEAN, "
                + "`startTime` VARCHAR NOT NULL, "
                + "`createdAt` VARCHAR NOT NULL, "
                + "`updatedAt` VARCHAR NOT NULL)");

    }
}
