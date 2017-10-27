package org.openlmis.core.persistence.migrations;

import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class CreatePTVProgramSchema extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `ptv_program` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`startPeriod` VARCHAR NOT NULL, "
                + "`endPeriod` VARCHAR NOT NULL, "
                + "`status` INTEGER NOT NULL, "
                + "`createdBy` VARCHAR NOT NULL, "
                + "`verifiedBy` VARCHAR NOT NULL, "
                + "`createdAt` VARCHAR NOT NULL, "
                + "`updatedAt` VARCHAR NOT NULL);");

        execSQL("CREATE TABLE `patient_dispensation` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`type` VARCHAR, "
                + "`total` BIGINT, "
                + "`ptvProgramId` INTEGER NOT NULL, "
                + "`createdAt` VARCHAR NOT NULL, "
                + "`updatedAt` VARCHAR NOT NULL, "
                + "FOREIGN KEY (ptvProgramId) REFERENCES ptv_program(id));");

        execSQL("CREATE TABLE `health_facility_service` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`name` VARCHAR NOT NULL, "
                + "`peripheral` INTEGER NOT NULL, "
                + "`createdAt` VARCHAR NOT NULL, "
                + "`updatedAt` VARCHAR NOT NULL);");

        execSQL("CREATE TABLE `ptv_program_stock_information` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`initialStock` BIGINT, "
                + "`entries` BIGINT, "
                + "`lossesAndAdjustments` BIGINT, "
                + "`requisition` BIGINT,"
                + "`ptvProgramId` INTEGER NOT NULL, "
                + "`productId` INTEGER NOT NULL, "
                + "`createdAt` VARCHAR NOT NULL, "
                + "`updatedAt` VARCHAR NOT NULL, "
                + "FOREIGN KEY (productId) REFERENCES product(id), "
                + "FOREIGN KEY (ptvProgramId) REFERENCES ptv_program(id));");

        execSQL("CREATE TABLE `service_dispensation` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`quantity` BIGINT, "
                + "`signature` VARCHAR, "
                + "`serviceId` INTEGER NOT NULL, "
                + "`ptvProgramStockInformationId` INTEGER NOT NULL, "
                + "`createdAt` VARCHAR NOT NULL, "
                + "`updatedAt` VARCHAR NOT NULL, "
                + "FOREIGN KEY (serviceId) REFERENCES health_facility_service(id), "
                + "FOREIGN KEY (ptvProgramStockInformationId) REFERENCES ptv_program_stock_information(id));");

        insertData();
    }

    private void insertData(){
        String formattedDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);

        execSQL("INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('1', 'CPN', '0', '" + formattedDate + "', '" + formattedDate + "');");
        execSQL("INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('2', 'Maternity', '0', '" + formattedDate + "', '" + formattedDate + "');");
        execSQL("INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('3', 'CCR', '0', '" + formattedDate + "', '" + formattedDate + "');");
        execSQL("INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('4', 'Pharmacy', '0', '" + formattedDate + "', '" + formattedDate + "');");
        execSQL("INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('5', 'UATS', '1', '" + formattedDate + "', '" + formattedDate + "');");
        execSQL("INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('6', 'Banco de socorro', '1', '" + formattedDate + "', '" + formattedDate + "');");
        execSQL("INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('7', 'Lab', '1', '" + formattedDate + "', '" + formattedDate + "');");
        execSQL("INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('8', 'Estomatologia', '1', '" + formattedDate + "', '" + formattedDate + "');");
    }
}
