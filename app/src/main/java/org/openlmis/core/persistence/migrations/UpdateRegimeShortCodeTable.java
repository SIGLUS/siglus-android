package org.openlmis.core.persistence.migrations;

import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

import static org.openlmis.core.model.Regimen.RegimeType.Adults;
import static org.openlmis.core.model.Regimen.RegimeType.Paediatrics;

public class UpdateRegimeShortCodeTable extends Migration {
    @Override
    public void up() {
        execSQL("ALTER TABLE 'regime_short_code' ADD COLUMN type VARCHAR");

        execSQL("DELETE FROM 'regime_short_code' WHERE id NOTNULL");

        String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S13', 'ABC+3TC+DTG', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S15', 'ABC+3TC+EFV', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S22', 'ABC+3TC+NVP', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S18Z', 'ABC+3TC+RAL', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S18', 'ABC+3TC+RAL+DRV+RTV', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S21', 'AZT+3TC+ABC', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S39Z', 'AZT+3TC+DTG', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S01', 'AZT+3TC+RAL+DRV+RTV', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S10', 'TDF+3TC+RAL+DRV+RTV', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S32Z', 'TDF+FTC PreEP', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S40Z', '2as Optimizadas ATV/r', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S23Z', '2as Optimizadas ATV/r+RAL', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S39B', '2as Optimizadas DRV+RTV', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S39Y', '3a Linha adaptada DRV+RAL+RTV', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S20', '3TC+RAL+DRV+RTV', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S19', 'AZT+3TC+ABC (2FDC+ABC Baby)', '" + formatDate + "' , '" + formatDate + "','" + Adults + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S01ZZ', 'ABC 60mg+3TC 30mg', '" + formatDate + "' , '" + formatDate + "','" + Paediatrics + "')");
        execSQL("INSERT INTO `regime_short_code` (`code` ,`shortCode`,`createdAt` ,`updatedAt`, `type` ) VALUES ('08S01B', 'ABC+3TC+DTG (2DFCped+DTG50)', '" + formatDate + "' , '" + formatDate + "','" + Paediatrics + "')");
    }
}
