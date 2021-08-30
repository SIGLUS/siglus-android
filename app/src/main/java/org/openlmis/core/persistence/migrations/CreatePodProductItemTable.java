package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePodProductItemTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `pod_product_items` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`pod_id` BIGINT NOT NULL, " +
        "`product_id` BIGINT, " +
        "`orderedQuantity` BIGINT NOT NULL, " +
        "`partialFulfilledQuantity` BIGINT NOT NULL, " +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL)");
  }

}
