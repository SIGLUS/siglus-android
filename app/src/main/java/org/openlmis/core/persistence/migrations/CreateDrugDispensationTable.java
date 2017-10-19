package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateDrugDispensationTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `drug_dispensation` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`quantity` INTEGER, "
                + "`signature` VARCHAR, "
                + "`departmentId` INTEGER, "
                + "`ptvProgramDepartmentId` INTEGER, "
                + "FOREIGN KEY (departmentId) REFERENCES department(id), "
                + "FOREIGN KEY (ptvProgramDepartmentId) REFERENCES ptv_program_department(id));");
    }
}
