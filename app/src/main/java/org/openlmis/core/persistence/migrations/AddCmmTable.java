package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddCmmTable extends Migration {

  @Override
  public void up() {
    execSQL("create table `cmm` (" +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
        "`stockCard_id` BIGINT," +
        "`cmmValue` REAL," +
        "`periodBegin` VARCHAR," +
        "`periodEnd` VARCHAR," +
        "`createdAt` VARCHAR NOT NULL," +
        "`updatedAt` VARCHAR NOT NULL," +
        "`synced` SMALLINT" +
        ")"
    );
  }
}
