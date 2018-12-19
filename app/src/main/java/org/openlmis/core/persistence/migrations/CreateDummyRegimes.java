/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.persistence.migrations;


import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class CreateDummyRegimes extends Migration {

    DbUtil dbUtil;


    public CreateDummyRegimes() {
        dbUtil = new DbUtil();
    }

    @Override
    public void up() {
        String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('019','Consultas AL US/APE Malaria 1x6','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('020','Consultas AL STOCK Malaria 1x6','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('021','Consultas AL US/APE Malaria 2x6','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('022','Consultas AL STOCK Malaria 2x6','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('023','Consultas AL US/APE Malaria 3x6','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('024','Consultas AL STOCK Malaria 3x6','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('025','Consultas AL US/APE Malaria 4x6','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('026','Consultas AL STOCK Malaria 4x6','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");

        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('PTV Crianças','PTV Crianças OpA+','Paediatrics','" + formatDate + "' , '" + formatDate + "' , '1')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ,`isCustom` ) VALUES ('PTV Mulheres','PTV Mulheres OpA+','Adults','" + formatDate + "' , '" + formatDate + "' , '1')");
    }
}
