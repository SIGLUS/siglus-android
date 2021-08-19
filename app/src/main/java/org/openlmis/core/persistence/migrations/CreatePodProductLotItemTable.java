package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePodProductLotItemTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `pod_product_lot_items` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`podProductItem_id` BIGINT NOT NULL, " +
        "`lot_id` BIGINT NOT NULL, " +
        "`shippedQuantity` BIGINT NOT NULL, " +
        "`acceptedQuantity` BIGINT, " +
        "`rejectedReason` VARCHAR, " +
        "`notes` VARCHAR, " +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL)");
  }
}
