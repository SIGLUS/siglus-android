package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePodTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `pods` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`shippedDate` VARCHAR, " +
        "`receivedDate` VARCHAR, " +
        "`deliveredBy` VARCHAR, " +
        "`receivedBy` VARCHAR, " +
        "`documentNo` VARCHAR," +
        "`orderCode` VARCHAR NOT NULL," +
        "`orderSupplyFacilityName` VARCHAR, " +
        "`orderStatus` VARCHAR NOT NULL, " +
        "`orderCreatedDate` VARCHAR, " +
        "`orderLastModifiedDate` VARCHAR, " +
        "`requisitionNumber` VARCHAR, " +
        "`requisitionIsEmergency` BOOLEAN DEFAULT 0, " +
        "`requisitionProgramCode` VARCHAR, " +
        "`requisitionStartDate` VARCHAR, " +
        "`requisitionEndDate` VARCHAR, " +
        "`requisitionActualStartDate` VARCHAR, " +
        "`requisitionActualEndDate` VARCHAR, " +
        "`isLocal` BOOLEAN DEFAULT 0, " +
        "`isDraft` BOOLEAN DEFAULT 0, " +
        "`isSynced` BOOLEAN DEFAULT 0, " +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL) ");
  }
}
