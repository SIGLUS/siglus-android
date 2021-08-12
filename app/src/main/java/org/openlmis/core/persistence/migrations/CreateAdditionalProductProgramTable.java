package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateAdditionalProductProgramTable extends Migration {


  @Override
  public void up() {
    execSQL("CREATE TABLE `additional_product_program` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
        "`programCode` VARCHAR NOT NULL," +
        "`productCode` VARCHAR NOT NULL, " +
        "`originProgramCode` VARCHAR NOT NULL," +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL)");
  }
}
