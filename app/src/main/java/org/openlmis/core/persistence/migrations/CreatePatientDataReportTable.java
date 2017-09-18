package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePatientDataReportTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `patient_data_report` (`reportedDate` VARCHAR NOT NULL,`id` INTEGER PRIMARY KEY AUTOINCREMENT, `createdAt` VARCHAR NOT NULL , `updatedAt` VARCHAR NOT NULL);");
    }
}
