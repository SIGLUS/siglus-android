package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePodProductTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `pod_products` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`pod_id ` BIGINT NOT NULL, " +
        "`code` VARCHAR NOT NULL, " +
        "`orderedQuantity` BIGINT NOT NULL, " +
        "`partialFulfilledQuantity` BIGINT NOT NULL )");

    execSQL("CREATE UNIQUE INDEX `pod_product_id_idx` ON `pod_products` ( `id` )");
  }

}
