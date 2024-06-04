package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddProvinceAndDistrictFieldsInUsersTable extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE `users` ADD COLUMN provinceCode VARCHAR");
    execSQL("ALTER TABLE `users` ADD COLUMN provinceName VARCHAR");
    execSQL("ALTER TABLE `users` ADD COLUMN districtCode VARCHAR");
    execSQL("ALTER TABLE `users` ADD COLUMN districtName VARCHAR");
  }
}