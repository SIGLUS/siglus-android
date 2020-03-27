package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class DirtyDataProductTable extends Migration {
    @Override
    public void up() {
        execSQL("create table `dirty_data` (" +
                "`createdAt` VARCHAR NOT NULL , " +
                "`updatedAt` VARCHAR NOT NULL , " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                "`json_data` VARCHAR NOT NULL," +
                "`sync_status` BOOLEAN DEFAULT 0," +
                "`productCode` VARCHAR NOT NULL" +
                ")"
        );
    }
}
