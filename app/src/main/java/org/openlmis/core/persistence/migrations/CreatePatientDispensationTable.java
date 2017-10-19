package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePatientDispensationTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `patient_dispensation` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`type` VARCHAR, "
                + "`total` INTEGER, "
                + "`ptvProgramId` INTEGER, "
                + "FOREIGN KEY (ptvProgramId) REFERENCES ptv_program(id));");
    }
}
