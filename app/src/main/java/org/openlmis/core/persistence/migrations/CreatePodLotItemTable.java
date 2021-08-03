package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePodLotItemTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `pod_lot_items` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`pod_product_code ` VARCHAR NOT NULL, " +
        "`lot_id` VARCHAR NOT NULL, " +
        "`shippedQuantity` BIGINT NOT NULL, " +
        "`acceptedQuantity` BIGINT, " +
        "`rejectedReason` VARCHAR, " +
        "`notes` VARCHAR)");

    execSQL("CREATE UNIQUE INDEX `pod_lot_item_id_idx` ON `pod_lot_items` ( `id` )");
  }
}
