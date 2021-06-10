package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddLotsTable extends Migration {

  @Override
  public void up() {
    execSQL("create table `lots` "
        + "(`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL, " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`product_id` BIGINT NOT NULL, " +
        "`lotNumber` VARCHAR NOT NULL, " +
        "`expirationDate` VARCHAR) ");
  }
}
