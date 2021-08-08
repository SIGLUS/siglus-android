package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePodLotItemTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `pod_lot_items` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`podProduct_id` VARCHAR NOT NULL, " +
        "`lot_id` VARCHAR NOT NULL, " +
        "`shippedQuantity` BIGINT NOT NULL, " +
        "`acceptedQuantity` BIGINT, " +
        "`rejectedReason` VARCHAR, " +
        "`notes` VARCHAR, " +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL)");
  }
}
