package org.openlmis.core.persistence.migrations;

import static org.openlmis.core.model.Regimen.RegimeType.Adults;
import static org.openlmis.core.model.Regimen.RegimeType.Paediatrics;

import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

public class UpdateRegimeShortCodeTable extends Migration {

  private static final String END = "')";

  @Override
  public void up() {
    execSQL("ALTER TABLE 'regime_short_code' ADD COLUMN type VARCHAR");

    execSQL("DELETE FROM 'regime_short_code' WHERE id NOTNULL");

    String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()),
        DateUtil.DATE_TIME_FORMAT);
    final String createAtAndUpdateAt = formatDate + "' , '" + formatDate + "','";
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus01', 'ABC+3TC+EFV', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus02', 'ABC+3TC+NVP', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus03', 'ABC+3TC+RAL', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus04', 'ABC+3TC+RAL+DRV+RTV', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus05', 'AZT+3TC+ABC', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus06', 'AZT+3TC+RAL+DRV+RTV', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus07', 'TDF+3TC+RAL+DRV+RTV', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus08', 'TDF+FTC PreEP', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus09', '2as Optimizadas ATV/r', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus10', '2as Optimizadas ATV/r+RAL', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus11', '2as Optimizadas DRV+RTV', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus12', '3a Linha adaptada DRV+RAL+RTV', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('AdultPlus13', '3TC+RAL+DRV+RTV', '"
            + createAtAndUpdateAt + Adults + END);
    execSQL(
        "INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('ChildrenPlus01', 'AZT+3TC+ABC (2FDC+ABC Baby)', '"
            + createAtAndUpdateAt + Paediatrics + END);
  }
}
