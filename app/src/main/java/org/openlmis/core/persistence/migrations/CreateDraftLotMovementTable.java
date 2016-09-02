package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateDraftLotMovementTable extends Migration {

    @Override
    public void up() {
        execSQL("CREATE TABLE `draft_lot_items` " +
                "(`quantity` BIGINT , " +
                "`createdAt` VARCHAR NOT NULL , " +
                "`updatedAt` VARCHAR NOT NULL , " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                "`draftInventory_id` BIGINT NOT NULL," +
                "`lot_id` BIGINT NOT NULL, " +
                "`newAdded` BOOLEAN DEFAULT 0) ");
        execSQL("CREATE UNIQUE INDEX `draft_lot_items_id_idx` ON `draft_lot_items` ( `id` )");
    }
}
