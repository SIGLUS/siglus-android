package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateRegimeThreeLineTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE IF NOT EXISTS `regime_three_lines` "
        + "(`form_id` BIGINT , "
        + "`regimeTypes` VARCHAR , "
        + "`patientsAmount` BIGINT , "
        + "`pharmacyAmount` BIGINT, "
        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "`createdAt` VARCHAR NOT NULL, "
        + "`updatedAt` VARCHAR NOT NULL )");
  }
}
