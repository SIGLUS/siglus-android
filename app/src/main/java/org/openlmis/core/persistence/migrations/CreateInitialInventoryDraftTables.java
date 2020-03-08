package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateInitialInventoryDraftTables extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `draft_initial_lot_items` " +
                "(`quantity` BIGINT , " +
                "`createdAt` VARCHAR NOT NULL , " +
                "`updatedAt` VARCHAR NOT NULL , " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                "`draftInitialInventory_id` BIGINT NOT NULL," +
                "`product_id` BIGINT NOT NULL, " +
                "`newAdded` BOOLEAN DEFAULT 0, " +
                "`lotNumber` VARCHAR NOT NULL, " +
                "`expirationDate` VARCHAR NOT NULL) ");
        execSQL("CREATE UNIQUE INDEX `draft_initial_lot_items_idx` ON `draft_initial_lot_items` ( `id` )");

        execSQL("CREATE TABLE `draft_initial_inventory` " +
                "(`expireDates` VARCHAR , " +
                "`quantity` BIGINT , " +
                "`product_id` BIGINT , " +
                "`createdAt` VARCHAR NOT NULL , " +
                "`updatedAt` VARCHAR NOT NULL , " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "done BOOLEAN DEFAULT 0)");
        execSQL("CREATE UNIQUE INDEX `draft_initial_inventory_idx` ON `draft_initial_inventory` ( `id` );");
    }
}
