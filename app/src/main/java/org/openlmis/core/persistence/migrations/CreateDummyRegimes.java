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

                    createRegime(dao, "AZT+3TC+NVP", Regimen.RegimeType.ADULT);
                    createRegime(dao, "TDF+3TC+EFV", Regimen.RegimeType.ADULT);
                    createRegime(dao, "AZT+3TC+EFV", Regimen.RegimeType.ADULT);
                    createRegime(dao, "d4T 30+3TC+NVP", Regimen.RegimeType.ADULT);
                    createRegime(dao, "d4T 30+3TC+EFV", Regimen.RegimeType.ADULT);
                    createRegime(dao, "AZT+3TC+LPV/r", Regimen.RegimeType.ADULT);
                    createRegime(dao, "TDF+3TC+LPV/r", Regimen.RegimeType.ADULT);
                    createRegime(dao, "ABC+3TC+LPV/r", Regimen.RegimeType.ADULT);

                    createRegime(dao, "d4T+3TC+NVP(3DFC Baby)", Regimen.RegimeType.BABY);
                    createRegime(dao, "d4T+3TC+LPV/r(2DFC Baby + LPV/r)", Regimen.RegimeType.BABY);
                    createRegime(dao, "d4T+3TC+ABC(2DFC Baby + ABC)", Regimen.RegimeType.BABY);
                    createRegime(dao, "d4T+3TC+EFV(2DFC Baby + EFV)", Regimen.RegimeType.BABY);
                    createRegime(dao, "AZT60+3TC+NVP(3DFC)", Regimen.RegimeType.BABY);
                    createRegime(dao, "AZT60+3TC+EFV(2DFC + EFV)", Regimen.RegimeType.BABY);
                    createRegime(dao, "AZT60+3TC+ABC(2DFC + ABC)", Regimen.RegimeType.BABY);
                    createRegime(dao, "AZT60+3TC+LPV/r(2DFC + LPV/r)", Regimen.RegimeType.BABY);
                    createRegime(dao, "ABC+3TC+LPV/r", Regimen.RegimeType.BABY);
                    createRegime(dao, "ABC+3TC+EFZ", Regimen.RegimeType.BABY);

                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    private void createRegime(Dao dao, String name, Regimen.RegimeType regimeType) throws SQLException {
        Regimen regimen = new Regimen();
        regimen.setName(name);
        regimen.setType(regimeType);
        dao.create(regimen);
    }

    @Override
    public void down(SQLiteDatabase db, ConnectionSource connectionSource) {

    }
}
