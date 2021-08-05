package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePodTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `pods` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, "+
        "`shippedDate` VARCHAR NOT NULL, " +
        "`receivedDate` VARCHAR, " +
        "`deliveredBy` VARCHAR, " +
        "`receivedBy` VARCHAR, " +
        "`documentNo` VARCHAR," +
        "`orderCode` VARCHAR NOT NULL," +
        "`orderSupplyFacilityName` VARCHAR NOT NULL, " +
        "`orderStatus` VARCHAR NOT NULL, " +
        "`orderCreatedDate` VARCHAR NOT NULL, " +
        "`orderLastModifiedDate` VARCHAR NOT NULL, " +
        "`requisitionNumber` VARCHAR NOT NULL, " +
        "`requisitionIsEmergency` BOOLEAN DEFAULT 0, " +
        "`requisitionProgramCode` VARCHAR NOT NULL, " +
        "`requisitionStartDate` VARCHAR NOT NULL, " +
        "`requisitionEndDate` VARCHAR NOT NULL, " +
        "`requisitionActualStartDate` VARCHAR NOT NULL, " +
        "`requisitionActualEndDate` VARCHAR NOT NULL) ");

    execSQL("CREATE UNIQUE INDEX `pod_id_idx` ON `pods` ( `id` )");
  }
}
