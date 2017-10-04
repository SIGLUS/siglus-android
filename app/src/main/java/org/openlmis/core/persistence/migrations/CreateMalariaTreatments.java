package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;


public class CreateMalariaTreatments extends Migration {

    @Override
    public void up() {
        execSQL("DROP TABLE `patient_data_report`;");
        execSQL("CREATE TABLE `malaria_program` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `reportedDate` VARCHAR NOT NULL, `startDatePeriod` VARCHAR NOT NULL, `endDatePeriod` VARCHAR NOT NULL, `statusMissing` INTEGER NOT NULL, `statusDraft` INTEGER NOT NULL, `statusComplete` INTEGER NOT NULL, `statusSynced` INTEGER NOT NULL, `createdAt` VARCHAR NOT NULL , `updatedAt` VARCHAR NOT NULL);");
        execSQL("CREATE TABLE `implementation` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `program` INTEGER NOT NULL, `executor` VARCHAR NOT NULL, `createdAt` VARCHAR NOT NULL , `updatedAt` VARCHAR NOT NULL, FOREIGN KEY (program) REFERENCES malaria_program(id));");
        execSQL("CREATE TABLE `treatment` (`id` INTEGER PRIMARY KEY AUTOINCREMENT,`implementation` INTEGER NOT NULL,`product` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `stock` INTEGER NOT NULL, `createdAt` VARCHAR NOT NULL , `updatedAt` VARCHAR NOT NULL, FOREIGN KEY (product) REFERENCES product(id), FOREIGN KEY (implementation) REFERENCES implementation(id));");

//        execSQL("Insert into malaria_program values (1,'2017-09-18 00:00:00','2017-09-18 00:00:00','2017-09-30 00:00:00',0,0,1,0,'2017-10-02 00:00:00','2017-10-02 00:00:00')");
//
//        execSQL("Insert into implementation values (1,1,'US','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//        execSQL("Insert into implementation values (2,1,'APE','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//
//        execSQL("Insert into treatment values (1, 1,'08O05','10','20','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//        execSQL("Insert into treatment values (2, 1,'08O05Z','10','20','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//        execSQL("Insert into treatment values (3, 1,'08O05Y','10','20','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//        execSQL("Insert into treatment values (4, 1,'08O05X','10','20','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//
//        execSQL("Insert into treatment values (5, 2,'08O05','10','20','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//        execSQL("Insert into treatment values (6, 2,'08O05Z','10','20','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//        execSQL("Insert into treatment values (7, 2,'08O05Y','10','20','2017-10-02 00:00:00','2017-10-02 00:00:00')");
//        execSQL("Insert into treatment values (8, 2,'08O05X','10','20','2017-10-02 00:00:00','2017-10-02 00:00:00')");

    }
}
