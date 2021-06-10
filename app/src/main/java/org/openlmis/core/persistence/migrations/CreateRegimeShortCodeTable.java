package org.openlmis.core.persistence.migrations;

import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

public class CreateRegimeShortCodeTable extends Migration {

  @Override
  public void up() {
    execSQL("CREATE TABLE `regime_short_code` "
        + "(`code` VARCHAR REFERENCES products(code), "
        + "`shortCode` VARCHAR, "
        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "`createdAt` VARCHAR NOT NULL, "
        + "`updatedAt` VARCHAR NOT NULL)");

    String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()),
        DateUtil.DATE_TIME_FORMAT);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S13', '3TC 150mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S15', 'AZT 300mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S22', 'NVP 200mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S18Z', 'TDF 300mg+3TC 300mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S18', 'TDF 300mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S21', 'EFV 600mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S39Z', 'Lpv/r 200/50mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S01', 'ABC 300mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S10', 'D4T 30mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S32Z', '3TC 30mg+D4T 6mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S40Z', '3TC 30mg+AZT 60mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S23Z', 'NVP 50mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S39B', 'Lpv/r 100/25mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S39Y', 'Lpv/r 80/20mL Solucao oral', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S20', 'EFV 200mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S19', 'EFV 50 mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S01ZZ', 'ABC 60mg+3TC 30mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S01B', 'ABC60mg', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S23', 'NVP 50mg/5ml sol oral', '"
            + formatDate + "' , '" + formatDate + "')");
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt` ) VALUES ('08S17', 'AZT 50mg/5ml sol oral', '"
            + formatDate + "' , '" + formatDate + "')");
  }
}
