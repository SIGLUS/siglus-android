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
        "`preparedBy` VARCHAR, " +
        "`conferredBy` VARCHAR, " +
        "`documentNo` VARCHAR, " +
        "`orderCode` VARCHAR NOT NULL, " +
        "`originOrderCode` VARCHAR, " +
        "`orderSupplyFacilityName` VARCHAR, " +
        "`orderSupplyFacilityDistrict` VARCHAR, " +
        "`orderSupplyFacilityProvince` VARCHAR, " +
        "`orderSupplyFacilityType` VARCHAR, " +
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
        "`processedDate` VARCHAR, " +
        "`serverProcessedDate` VARCHAR, " +
        "`stockManagementReason` VARCHAR, " +
        "`isLocal` BOOLEAN DEFAULT 0, " +
        "`isDraft` BOOLEAN DEFAULT 0, " +
        "`isSynced` BOOLEAN DEFAULT 0, " +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL) ");
  }
}
