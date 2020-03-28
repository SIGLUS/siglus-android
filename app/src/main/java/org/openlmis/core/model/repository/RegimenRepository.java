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
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimeShortCode;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

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
        return dbUtil.withDao(Regimen.class, new DbUtil.Operation<Regimen, Regimen>() {
            @Override
            public Regimen operate(Dao<Regimen, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("name", name).and().eq("type", category).queryForFirst();
            }
        });
    }

    public Regimen getByCode(final String code) throws LMISException {
        return dbUtil.withDao(Regimen.class, new DbUtil.Operation<Regimen, Regimen>() {
            @Override
            public Regimen operate(Dao<Regimen, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("code", code).queryForFirst();
            }
        });
    }

    public void deleteAllNoCustomRegimens() throws LMISException, SQLException {
        TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return dbUtil.withDao(Regimen.class, new DbUtil.Operation<Regimen, Regimen>() {
                    @Override
                    public Regimen operate(Dao<Regimen, String> dao) throws SQLException, LMISException {
                        DeleteBuilder<Regimen, String> deleteBuilder = dao.deleteBuilder();
                        deleteBuilder.where().eq("isCustom", false);
                        deleteBuilder.delete();
                        return null;
                    }
                });
            }
        });
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
        return dbUtil.withDao(Regimen.class, new DbUtil.Operation<Regimen, List<Regimen>>() {
            @Override
            public List<Regimen> operate(Dao<Regimen, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .where()
                        .eq("isCustom", false)
                        .query();
            }
        });
    }

    public List<RegimeShortCode> listRegimeShortCode(Regimen.RegimeType type) throws LMISException {
        return dbUtil.withDao(RegimeShortCode.class, new DbUtil.Operation<RegimeShortCode, List<RegimeShortCode>>() {
            @Override
            public List<RegimeShortCode> operate(Dao<RegimeShortCode, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .where()
                        .eq("type", type)
                        .query();
            }
        });
    }

    public void deleteRegimeDirtyData(String productCode){
        String deleteRegimeThreeLines="DELETE FROM regime_three_lines "
                + "WHERE form_id=(SELECT id FROM rnr_forms WHERE status='DRAFT_MISSED' AND program_id=(SELECT id FROM"
                + "programs WHERE programCode=(SELECT programCode FROM product_programs WHERE productCode='"+productCode+"'));";
        String deleteRegimeItems="DELETE FROM regime_items "
                + "WHERE form_id=(SELECT id FROM rnr_forms WHERE status='DRAFT_MISSED' AND program_id=(SELECT id FROM"
                + "programs WHERE programCode=(SELECT programCode FROM product_programs WHERE productCode='"+productCode+"'));";
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRegimeThreeLines);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRegimeItems);
    }
}
