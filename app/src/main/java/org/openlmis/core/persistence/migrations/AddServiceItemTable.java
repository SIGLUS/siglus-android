package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddServiceItemTable extends Migration {

  @Override
  public void up() {

    execSQL("create table `service_items` "
        + "(`formItem_id` BIGINT , "
        + "`service_id` BIGINT,"
        + "`amount` BIGINT,"
        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "`createdAt` VARCHAR NOT NULL, "
        + "`updatedAt` VARCHAR NOT NULL)");
  }
}
