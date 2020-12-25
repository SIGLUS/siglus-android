package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateDirtyDataProductTable extends Migration {
    @Override
    public void up() {
        execSQL("create table `dirty_data` (" +
                " `id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                " `productCode` VARCHAR NOT NULL," +
                " `jsonData` VARCHAR NOT NULL," +
                " `synced` BOOLEAN DEFAULT 0," +
                " `fullyDelete` BOOLEAN DEFAULT 1," +
                " `createdAt` VARCHAR NOT NULL , " +
                " `updatedAt` VARCHAR NOT NULL" +
                ")"
        );
    }
}
