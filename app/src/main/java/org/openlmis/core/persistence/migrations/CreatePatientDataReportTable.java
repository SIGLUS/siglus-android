package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePatientDataReportTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `patient_data_report` ( " +
        "`reportedDate` VARCHAR NOT NULL, " +
        "`startDatePeriod` VARCHAR NOT NULL, " +
        "`endDatePeriod` VARCHAR NOT NULL, " +
        "`type` VARCHAR NOT NULL, " +
        "`currentTreatment6x1` NUMERIC NOT NULL, " +
        "`currentTreatment6x2` NUMERIC NOT NULL, " +
        "`currentTreatment6x3` NUMERIC NOT NULL, " +
        "`currentTreatment6x4` NUMERIC NOT NULL, " +
        "`existingStock6x1` NUMERIC NOT NULL, " +
        "`existingStock6x2` NUMERIC NOT NULL, " +
        "`existingStock6x3` NUMERIC NOT NULL, " +
        "`existingStock6x4` NUMERIC NOT NULL, " +
        "`statusMissing` INTEGER NOT NULL, " +
        "`statusDraft` INTEGER NOT NULL, " +
        "`statusComplete` INTEGER NOT NULL, " +
        "`statusSynced` INTEGER NOT NULL, " +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL);");
  }
}
