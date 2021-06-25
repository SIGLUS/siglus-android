package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateBulkEntriesDraftTables extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `draft_bulk_entries_product_lot_item` " +
        "(`quantity` BIGINT , " +
        "`lotSoh` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
        "`draftBulkEntriesProduct_id` BIGINT NOT NULL," +
        "`product_id` BIGINT NOT NULL, " +
        "`newAdded` BOOLEAN DEFAULT 0, " +
        "`lotNumber` VARCHAR NOT NULL, " +
        "`documentNumber` VARCHAR , " +
        "`reason` VARCHAR NOT NULL , " +
        "`expirationDate` VARCHAR NOT NULL) ");
    execSQL(
        "CREATE UNIQUE INDEX `draft_bulk_entries_product_lot_item_idx` ON `draft_bulk_entries_product_lot_item` ( `id` )");

    execSQL("CREATE TABLE `draft_bulk_entries_product` " +
        "( `quantity` BIGINT , " +
        "`product_id` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT , " +
        "done BOOLEAN DEFAULT 0)");
    execSQL(
        "CREATE UNIQUE INDEX `draft_bulk_entries_product_idx` ON `draft_bulk_entries_product` ( `id` );");
  }
}
