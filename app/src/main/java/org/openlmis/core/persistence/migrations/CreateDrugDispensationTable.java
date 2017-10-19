package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateDrugDispensationTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `drug_dispensation` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`quantity` INTEGER, "
                + "`signature` VARCHAR, "
                + "`serviceId` INTEGER, "
                + "`ptvProgramProductId` INTEGER, "
                + "FOREIGN KEY (serviceId) REFERENCES service(id), "
                + "FOREIGN KEY (ptvProgramProductId) REFERENCES ptv_program_product(id));");
    }
}
