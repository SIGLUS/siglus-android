package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateBulkIssueDraftTables extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `draft_bulk_issue_products` " +
        "( `requested` BIGINT , " +
        "`product_id` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT , " +
        "`movementReasonCode` VARCHAR NOT NULL , " +
        "`documentNumber` VARCHAR , " +
        "`done` BOOLEAN DEFAULT 0)");

    execSQL("CREATE TABLE `draft_bulk_issue_lots` " +
        "(`amount` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
        "`draftBulkIssueProduct_id` BIGINT NOT NULL," +
        "`lotOnHand_id` BIGINT NOT NULL) " );
  }
}
