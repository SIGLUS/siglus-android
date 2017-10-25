package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;


public class CreateMalariaTreatments extends Migration {

    @Override
    public void up() {
        execSQL("DROP TABLE `patient_data_report`;");
        execSQL("CREATE TABLE `malaria_program` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `reportedDate` VARCHAR NOT NULL, `startDatePeriod` VARCHAR NOT NULL, `endDatePeriod` VARCHAR NOT NULL, `statusMissing` INTEGER NOT NULL, `statusDraft` INTEGER NOT NULL, `statusComplete` INTEGER NOT NULL, `statusSynced` INTEGER NOT NULL, `createdAt` VARCHAR NOT NULL , `updatedAt` VARCHAR NOT NULL);");
        execSQL("CREATE TABLE `implementation` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `program` INTEGER NOT NULL, `executor` VARCHAR NOT NULL, `createdAt` VARCHAR NOT NULL , `updatedAt` VARCHAR NOT NULL, FOREIGN KEY (program) REFERENCES malaria_program(id));");
        execSQL("CREATE TABLE `treatment` (`id` INTEGER PRIMARY KEY AUTOINCREMENT,`implementation` INTEGER NOT NULL,`product` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `stock` INTEGER NOT NULL, `createdAt` VARCHAR NOT NULL , `updatedAt` VARCHAR NOT NULL, FOREIGN KEY (product) REFERENCES product(id), FOREIGN KEY (implementation) REFERENCES implementation(id));");

    }
}
