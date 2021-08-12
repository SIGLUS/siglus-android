package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateUsageColumnsMapTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `usage_columns_map` " +
        "(`code` VARCHAR NOT NULL, " +
        "`testProject` VARCHAR , " +
        "`testOutcome` VARCHAR , " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL , " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT )");
  }
}
