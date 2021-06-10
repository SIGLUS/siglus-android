package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateKitProductsTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `kit_products` "
        + "(`kitCode` VARCHAR REFERENCES products(code), "
        + "`productCode` VARCHAR REFERENCES products(code), "
        + "`quantity` BIGINT, "
        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "`createdAt` VARCHAR NOT NULL, "
        + "`updatedAt` VARCHAR NOT NULL)");
  }
}
