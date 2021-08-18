package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateTestConsumptionItemsTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `test_consumption_items` " +
        "(`usageColumnsMap_id` BIGINT NOT NULL, " +
        "`form_id` BIGINT , " +
        "`service` VARCHAR , " +
        "`value` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT )");
  }
}
