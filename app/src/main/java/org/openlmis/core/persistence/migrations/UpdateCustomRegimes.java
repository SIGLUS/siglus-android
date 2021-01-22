package org.openlmis.core.persistence.migrations;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class UpdateCustomRegimes extends Migration{
    DbUtil dbUtil;


    public UpdateCustomRegimes() {
        dbUtil = new DbUtil();
    }

    @Override
    public void up() {
        String formatDate = DateUtil.formatDate(DateUtil.getCurrentDate(), DateUtil.DATE_TIME_FORMAT);

        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('AL US/APE Malaria 1x6','Consultas AL US/APE Malaria 1x6','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('AL STOCK Malaria 1x6','Consultas AL STOCK Malaria 1x6','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('AL US/APE Malaria 2x6','Consultas AL US/APE Malaria 2x6','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('AL STOCK Malaria 2x6','Consultas AL STOCK Malaria 2x6','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('AL US/APE Malaria 3x6','Consultas AL US/APE Malaria 3x6','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('AL STOCK Malaria 3x6','Consultas AL STOCK Malaria 3x6','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('AL US/APE Malaria 4x6','Consultas AL US/APE Malaria 4x6','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('AL STOCK Malaria 4x6','Consultas AL STOCK Malaria 4x6','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");

        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('PTV Crianças','PTV Crianças OpA+','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('PTV Mulheres','PTV Mulheres OpA+','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");
    }

}

