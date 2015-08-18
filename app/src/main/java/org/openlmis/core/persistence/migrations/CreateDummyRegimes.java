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

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.Migration;

import java.sql.SQLException;

public class CreateDummyRegimes implements Migration {

    DbUtil dbUtil;

    public CreateDummyRegimes() {
        dbUtil = new DbUtil();
    }

    @Override
    public void up(SQLiteDatabase db, ConnectionSource connectionSource) {

        try {
            dbUtil.withDao(Regimen.class, new DbUtil.Operation<Regimen, String>() {
                @Override
                public String operate(Dao dao) throws SQLException {
                    for (int i = 0; i < 7; i++) {
                        if (i==3 || i== 6){
                            continue;
                        }

                        Regimen regimen = new Regimen();
                        regimen.setCode("00" + (i+1));

                        regimen.setName("AZT+3TC+NVP-" + i);
                        if (i % 2 == 0) {
                            regimen.setType(Regimen.RegimeType.ADULT);
                        } else {
                            regimen.setType(Regimen.RegimeType.BABY);
                        }
                        dao.create(regimen);
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void down(SQLiteDatabase db, ConnectionSource connectionSource) {

    }
}
