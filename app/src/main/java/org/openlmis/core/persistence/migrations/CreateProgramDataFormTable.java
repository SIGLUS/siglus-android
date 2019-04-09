package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProgramDataFormTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `program_data_forms` " +
                "(`submittedTime` VARCHAR , " +
                "`periodBegin` VARCHAR , " +
                "`periodEnd` VARCHAR , " +
                "`program_id` BIGINT , " +
                "`status` VARCHAR DEFAULT 'DRAFT' , " +
                "`synced` SMALLINT , " +
                "`createdAt` VARCHAR NOT NULL , " +
                "`updatedAt` VARCHAR NOT NULL , " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT )");
    }
}
