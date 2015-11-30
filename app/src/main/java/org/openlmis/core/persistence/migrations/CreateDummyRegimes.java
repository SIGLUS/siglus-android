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


import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.Migration;

import java.sql.SQLException;

public class CreateDummyRegimes extends Migration {

    DbUtil dbUtil;


    public CreateDummyRegimes(){
        dbUtil = new DbUtil();
    }

    @Override
    public void up() {

        try {
            dbUtil.withDao(Regimen.class, new DbUtil.Operation<Regimen, String>() {
                @Override
                public String operate(Dao dao) throws SQLException {

                    createRegime(dao, "001","AZT+3TC+NVP", Regimen.RegimeType.ADULT);
                    createRegime(dao, "002","TDF+3TC+EFV", Regimen.RegimeType.ADULT);
                    createRegime(dao, "003","AZT+3TC+EFV", Regimen.RegimeType.ADULT);
                    createRegime(dao, "004","d4T 30+3TC+NVP", Regimen.RegimeType.ADULT);
                    createRegime(dao, "005","d4T 30+3TC+EFV", Regimen.RegimeType.ADULT);
                    createRegime(dao, "006","AZT+3TC+LPV/r", Regimen.RegimeType.ADULT);
                    createRegime(dao, "007","TDF+3TC+LPV/r", Regimen.RegimeType.ADULT);
                    createRegime(dao, "008","ABC+3TC+LPV/r", Regimen.RegimeType.ADULT);

                    createRegime(dao, "009","d4T+3TC+NVP(3DFC Baby)", Regimen.RegimeType.BABY);
                    createRegime(dao, "010","d4T+3TC+LPV/r(2DFC Baby + LPV/r)", Regimen.RegimeType.BABY);
                    createRegime(dao, "011","d4T+3TC+ABC(2DFC Baby + ABC)", Regimen.RegimeType.BABY);
                    createRegime(dao, "012","d4T+3TC+EFV(2DFC Baby + EFV)", Regimen.RegimeType.BABY);
                    createRegime(dao, "013","AZT60+3TC+NVP(3DFC)", Regimen.RegimeType.BABY);
                    createRegime(dao, "014","AZT60+3TC+EFV(2DFC + EFV)", Regimen.RegimeType.BABY);
                    createRegime(dao, "015","AZT60+3TC+ABC(2DFC + ABC)", Regimen.RegimeType.BABY);
                    createRegime(dao, "016","AZT60+3TC+LPV/r(2DFC + LPV/r)", Regimen.RegimeType.BABY);
                    createRegime(dao, "017","ABC+3TC+LPV/r", Regimen.RegimeType.BABY);
                    createRegime(dao, "018","ABC+3TC+EFZ", Regimen.RegimeType.BABY);

                    return null;
                }
            });
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    private void createRegime(Dao dao, String code ,String name, Regimen.RegimeType regimeType) throws SQLException {
        Regimen regimen = new Regimen();
        regimen.setCode(code);
        regimen.setName(name);
        regimen.setType(regimeType);
        dao.create(regimen);
    }

    @Override
    public void down() {

    }
}
