package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddInventoryTable extends Migration {
    @Override
    public void up() {
        execSQL("create table `inventory` "
                + "(`createdAt` VARCHAR NOT NULL, `updatedAt` VARCHAR NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT) ");
    }
}
