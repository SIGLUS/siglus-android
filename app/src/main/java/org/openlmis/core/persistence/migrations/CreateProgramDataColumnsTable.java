package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProgramDataColumnsTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `program_data_columns` " +
        "(`code` VARCHAR NOT NULL, " +
        "`label` VARCHAR , " +
        "`description` VARCHAR , " +
        "`program_id` BIGINT, " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT )");
  }
}
