package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateIssueVoucherDraftTables extends Migration {

  @Override
  public void up() {

    execSQL("CREATE TABLE `draft_issue_voucher_product_items` " +
        "(`pod_id` BIGINT, " +
        "`product_id` BIGINT, " +
        "`done` BOOLEAN DEFAULT 0, " +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL, " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT) ");

    execSQL("CREATE TABLE `draft_issue_voucher_product_lot_items` " +
        "(`draftIssueVoucherProductItem_id` BIGINT NOT NULL, " +
        "`shippedQuantity` BIGINT, " +
        "`acceptedQuantity` BIGINT, " +
        "`lotNumber` VARCHAR NOT NULL, " +
        "`expirationDate` VARCHAR NOT NULL, " +
        "`newAdded` BOOLEAN DEFAULT 0, " +
        "`done` BOOLEAN DEFAULT 0, " +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL, " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT) ");
  }
}
