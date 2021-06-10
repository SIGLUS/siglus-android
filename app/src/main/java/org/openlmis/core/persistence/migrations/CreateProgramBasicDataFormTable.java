package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProgramBasicDataFormTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `program_data_Basic_items` " +
        "(`code` VARCHAR , " +
        "`product_id` BIGINT NOT NULL, " +
        "`form_id` BIGINT , " +
        "`isCustomAmount` BOOLEAN DEFAULT 0, " +
        "`name` VARCHAR , " +
        "`initialAmount` BIGINT , " +
        "`received` BIGINT , " +
        "`issued` BIGINT , " +
        "`adjustment` BIGINT , " +
        "`validate` VARCHAR , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
        "`inventory` BIGINT  )");
  }
}
