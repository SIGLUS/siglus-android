package org.openlmis.core.persistence.migrations;

import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

@SuppressWarnings("PMD")
public class CreatePTVProgramSchema extends Migration {

  public static final String ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT = "`id` INTEGER PRIMARY KEY AUTOINCREMENT, ";
  public static final String CREATED_AT = "`createdAt` VARCHAR NOT NULL, ";
  public static final String UPDATED_AT = "`updatedAt` VARCHAR NOT NULL, ";

  @Override
  public void up() {
    execSQL("CREATE TABLE IF NOT EXISTS `ptv_program` ("
        + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
        + "`startPeriod` VARCHAR NOT NULL, "
        + "`endPeriod` VARCHAR NOT NULL, "
        + "`status` INTEGER NOT NULL, "
        + "`createdBy` VARCHAR NOT NULL, "
        + "`verifiedBy` VARCHAR NOT NULL, "
        + CREATED_AT
        + "`updatedAt` VARCHAR NOT NULL,"
        + "CONSTRAINT unique_period_cst"
        + " UNIQUE (startPeriod, endPeriod) ON CONFLICT REPLACE);");

    execSQL("CREATE TABLE IF NOT EXISTS `patient_dispensation` ("
        + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
        + "`type` VARCHAR, "
        + "`total` BIGINT, "
        + "`ptvProgramId` INTEGER NOT NULL, "
        + CREATED_AT
        + UPDATED_AT
        + "FOREIGN KEY (ptvProgramId) REFERENCES ptv_program(id));");

    execSQL("CREATE TABLE IF NOT EXISTS `health_facility_service` ("
        + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
        + "`name` VARCHAR NOT NULL, "
        + "`peripheral` INTEGER NOT NULL, "
        + CREATED_AT
        + "`updatedAt` VARCHAR NOT NULL);");

    execSQL("CREATE TABLE IF NOT EXISTS `ptv_program_stock_information` ("
        + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
        + "`initialStock` BIGINT, "
        + "`entries` BIGINT, "
        + "`lossesAndAdjustments` BIGINT, "
        + "`requisition` BIGINT,"
        + "`ptvProgramId` INTEGER NOT NULL, "
        + "`productId` INTEGER NOT NULL, "
        + CREATED_AT
        + UPDATED_AT
        + "FOREIGN KEY (productId) REFERENCES product(id), "
        + "FOREIGN KEY (ptvProgramId) REFERENCES ptv_program(id));");

    execSQL("CREATE TABLE IF NOT EXISTS `service_dispensation` ("
        + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
        + "`quantity` BIGINT, "
        + "`signature` VARCHAR, "
        + "`serviceId` INTEGER NOT NULL, "
        + "`ptvProgramStockInformationId` INTEGER NOT NULL, "
        + CREATED_AT
        + UPDATED_AT
        + "FOREIGN KEY (serviceId) REFERENCES health_facility_service(id), "
        + "FOREIGN KEY (ptvProgramStockInformationId) REFERENCES ptv_program_stock_information(id));");

    insert();
  }

  private void insert() {
    execSQL("DELETE FROM health_facility_service ");
    String formattedDate = DateUtil
        .formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);
    execSQL(
        "INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('1', 'CPN', '0', '"
            + formattedDate + "', '" + formattedDate + "');");
    execSQL(
        "INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('2', 'Maternity', '0', '"
            + formattedDate + "', '" + formattedDate + "');");
    execSQL(
        "INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('3', 'CCR', '0', '"
            + formattedDate + "', '" + formattedDate + "');");
    execSQL(
        "INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('4', 'Pharmacy', '0', '"
            + formattedDate + "', '" + formattedDate + "');");
    execSQL(
        "INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('5', 'UATS', '1', '"
            + formattedDate + "', '" + formattedDate + "');");
    execSQL(
        "INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('6', 'Banco de socorro', '1', '"
            + formattedDate + "', '" + formattedDate + "');");
    execSQL(
        "INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('7', 'Lab', '1', '"
            + formattedDate + "', '" + formattedDate + "');");
    execSQL(
        "INSERT INTO health_facility_service (`id`, `name`, `peripheral`, `createdAt`, `updatedAt`) values ('8', 'Estomatologia', '1', '"
            + formattedDate + "', '" + formattedDate + "');");
  }
}
