package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateBulkIssueDraftTables extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `draft_bulk_issue_product` " +
        "( `requested` BIGINT , " +
        "`product_id` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT , " +
        "`done` BOOLEAN DEFAULT 0)");

    execSQL("CREATE TABLE `draft_bulk_issue_product_lot_item` " +
        "(`amount` BIGINT , " +
        "`lotSoh` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
        "`draftBulkIssueProduct_id` BIGINT NOT NULL," +
        "`lotNumber` VARCHAR NOT NULL, " +
        "`expirationDate` VARCHAR NOT NULL) ");
  }
}
