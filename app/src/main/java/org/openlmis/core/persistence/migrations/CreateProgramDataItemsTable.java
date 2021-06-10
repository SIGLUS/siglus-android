package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProgramDataItemsTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `program_data_items` " +
        "(`programDataColumn_id` BIGINT NOT NULL, " +
        "`form_id` BIGINT , " +
        "`name` VARCHAR , " +
        "`value` BIGINT , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT )");
  }
}
