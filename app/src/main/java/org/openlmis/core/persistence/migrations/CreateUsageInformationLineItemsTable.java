package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateUsageInformationLineItemsTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `usage_information_line_item` " +
        "(`usageColumnsMap_id` BIGINT NOT NULL, " +
        "`form_id` BIGINT , " +
        "`service` VARCHAR , " +
        "`value` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT )");
  }
}
