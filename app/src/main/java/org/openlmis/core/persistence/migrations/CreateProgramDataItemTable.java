package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProgramDataItemTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `program_data_items` " +
                "(`program_data_column_code` VARCHAR , " +
                "`name` VARCHAR , " +
                "`value` BIGINT , " +
                "`createdAt` VARCHAR NOT NULL , " +
                "`updatedAt` VARCHAR NOT NULL , " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT )");
    }
}
