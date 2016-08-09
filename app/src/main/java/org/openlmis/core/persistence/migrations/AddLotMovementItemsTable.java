package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddLotMovementItemsTable extends Migration {
    @Override
    public void up() {
        execSQL("create table `lot_movement_items` "
                + "(`createdAt` VARCHAR NOT NULL, " +
                "`updatedAt` VARCHAR NOT NULL, " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`lot_id` BIGINT NOT NULL, " +
                "`stockOnHand` BIGINT NOT NULL, " +
                "`movementQuantity` BIGINT NOT NULL, " +
                "`stockMovementItem_id` BIGINT NOT NULL) ");
    }
}