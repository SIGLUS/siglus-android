package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreatePTVProgramDepartmentTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `ptv_program_department` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`initialStock` INTEGER, "
                + "`entries` INTEGER, "
                + "`lossesAndAdjustments` INTEGER, "
                + "`requisition` INTEGER,"
                + "`ptvProgramId` INTEGER, "
                + "`productId` INTEGER, "
                + "FOREIGN KEY (productId) REFERENCES product(id),"
                + "FOREIGN KEY (ptvProgramId) REFERENCES ptv_program(id));");
    }
}
