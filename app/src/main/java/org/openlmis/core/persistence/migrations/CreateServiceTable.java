package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateServiceTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE IF NOT EXISTS `services` "
        + "(`name` VARCHAR , "
        + "`code` VARCHAR , "
        + "`program_id` BIGINT , "
        + "`active` BOOLEAN, "
        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT , "
        + "`createdAt` VARCHAR NOT NULL , "
        + "`updatedAt` VARCHAR NOT NULL)");
  }
}
