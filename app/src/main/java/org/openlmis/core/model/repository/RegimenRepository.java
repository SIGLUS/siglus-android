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

package org.openlmis.core.model.repository;


import android.content.Context;


import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimeShortCode;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.util.List;

public class RegimenRepository {

    GenericDao<Regimen> regimenGenericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    Context context;

    @Inject
    public RegimenRepository(Context context) {
        this.regimenGenericDao = new GenericDao<>(Regimen.class, context);
    }

    public Regimen getByNameAndCategory(final String name, final Regimen.RegimeType category) throws LMISException {
        return dbUtil.withDao(Regimen.class, dao -> dao.queryBuilder().where().eq("name", name).and().eq("type", category).queryForFirst());
    }

    public Regimen getByCode(final String code) throws LMISException {
        return dbUtil.withDao(Regimen.class, dao -> dao.queryBuilder().where().eq("code", code).queryForFirst());
    }

    public void batchSave(List<Regimen> regimens) {
        try {
            for (Regimen regimen : regimens) {
                if (!regimen.isActive()) continue;
                createOrUpdate(regimen);
            }
        } catch (LMISException e) {
            new LMISException(e, "RegimenRepository.batchSave").reportToFabric();
        }
    }

    private void createOrUpdate(Regimen regimen) throws LMISException {
        Regimen existingRegimen = getByCode(regimen.getCode());
        if (existingRegimen == null) {
            create(regimen);
        } else {
            regimen.setId(existingRegimen.getId());
            regimenGenericDao.update(regimen);
        }
    }

    public void create(Regimen regimen) throws LMISException {
        regimenGenericDao.create(regimen);
    }

    public List<Regimen> listDefaultRegime() throws LMISException {
        return dbUtil.withDao(Regimen.class, dao -> dao.queryBuilder()
                .where()
                .eq("isCustom", false)
                .and().eq("active", true)
                .query());
    }

    public List<RegimeShortCode> listRegimeShortCode(Regimen.RegimeType type) throws LMISException {
        return dbUtil.withDao(RegimeShortCode.class, dao -> dao.queryBuilder()
                .where()
                .eq("type", type)
                .query());
    }

    public void deleteRegimeDirtyData(String programCode) {
        String deleteRegimeThreeLines = "DELETE FROM regime_three_lines "
                + "WHERE form_id=(SELECT id FROM rnr_forms WHERE synced=0 AND program_id=(SELECT id FROM programs "
                + "WHERE programCode='" + programCode + "'));";
        String deleteRegimeItems = "DELETE FROM regime_items "
                + "WHERE form_id=(SELECT id FROM rnr_forms WHERE synced=0 AND program_id=(SELECT id FROM programs "
                + "WHERE programCode='" + programCode + "'));";
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRegimeThreeLines);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRegimeItems);
    }
}
