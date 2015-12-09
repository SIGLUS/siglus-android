package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddSyncErrorsMessageTable extends Migration {
    @Override
    public void up() {
        execSQL("create table `sync_errors` " +
                "(`createdAt` VARCHAR NOT NULL, `updatedAt` VARCHAR NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`errorMessage` VARCHAR , `syncType` VARCHAR , `syncObjectId` INTEGER) ");
    }
}
