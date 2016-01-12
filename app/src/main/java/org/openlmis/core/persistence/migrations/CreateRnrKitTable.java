package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateRnrKitTable extends Migration{

    @Override
    public void up() {
        execSQL("CREATE TABLE `rnr_kit_items` " +
                "(`kitsReceived` BIGINT, " +
                "`kitsOpened` BIGINT, " +
                "`kitCode` VARCHAR NOT NULL REFERENCES products(code)," +
                "`form_id` BIGINT NOT NULL, " +
                "`createdAt` VARCHAR NOT NULL, " +
                "`updatedAt` VARCHAR NOT NULL, " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT)");
    }
}
