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
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('001','AZT+3TC+NVP','Adults', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('002','TDF+3TC+EFV','Adults', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('003','AZT+3TC+EFV','Adults', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('004','d4T 30+3TC+NVP','Adults', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('005','d4T 30+3TC+EFV','Adults', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('006','AZT+3TC+LPV/r','Adults', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('007','TDF+3TC+LPV/r','Adults', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('008','ABC+3TC+LPV/r','Adults', '" + formatDate + "' , '" + formatDate + "')");

        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('009','d4T+3TC+NVP(3DFC Baby)','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('010','d4T+3TC+LPV/r(2DFC Baby + LPV/r)','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('011','d4T+3TC+ABC(2DFC Baby + ABC)','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('012','d4T+3TC+EFV(2DFC Baby + EFV)','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('013','AZT60+3TC+NVP(3DFC)','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('014','AZT60+3TC+EFV(2DFC + EFV)','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('015','AZT60+3TC+ABC(2DFC + ABC)','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('016','AZT60+3TC+LPV/r(2DFC + LPV/r)','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('017','ABC+3TC+LPV/r','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
        execSQL("INSERT INTO `regimes` (`code` ,`name` ,`type` ,`createdAt` ,`updatedAt` ) VALUES ('018','ABC+3TC+EFZ','Paediatrics', '" + formatDate + "' , '" + formatDate + "')");
    }
}
