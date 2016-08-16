package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddLotOnHandTable extends Migration {
    @Override
    public void up() {
        execSQL("create table `lots_on_hand` "
                + "(`createdAt` VARCHAR NOT NULL, "
                + "`updatedAt` VARCHAR NOT NULL, "
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`lot_id` BIGINT NOT NULL, "
                + "`quantityOnHand` BIGINT NOT NULL, "
                + "`stockCard_id` BIGINT NOT NULL) ");
    }
}
