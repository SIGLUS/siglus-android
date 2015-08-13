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

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class RnrFormRepository {

    private static final String TAG = "RnrFormRepository";

    @Inject
    DbUtil dbUtil;

    @Inject
    StockRepository stockRepository;

    @Inject
    RegimenRepository regimenRepository;

    GenericDao<RnRForm> genericDao;
    GenericDao<RnrFormItem> rnrFormItemGenericDao;

    @Inject
    public RnrFormRepository(Context context){
        genericDao = new GenericDao<>(RnRForm.class, context);
        rnrFormItemGenericDao = new GenericDao<>(RnrFormItem.class, context);
    }


    public void create(RnRForm rnRForm) throws LMISException{
        genericDao.create(rnRForm);
    }

    public void save(RnRForm form) throws LMISException, SQLException {
        updateRegimenAndBaseInfo(form);
        genericDao.update(form);
    }

    public void updateRegimenAndBaseInfo(RnRForm form) throws SQLException {
        for (RegimenItem item : form.getRegimenItemListWrapper()) {
            form.getRegimenItemList().update(item);
        }
        for (BaseInfoItem item : form.getBaseInfoItemListWrapper()) {
            form.getBaseInfoItemList().update(item);
        }
    }

    public List<RnRForm> list() throws LMISException {
        return genericDao.queryForAll();
    }

    public List<RnRForm> listUnSynced() throws LMISException{
        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, List<RnRForm>>() {
            @Override
            public List<RnRForm> operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("synced", false).and().eq("status", RnRForm.STATUS.AUTHORIZED).query();
            }
        });
    }


    public void approve(RnRForm form) throws LMISException{
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        genericDao.update(form);
    }

    public void createRnrFormItems(final List<RnrFormItem> rnrFormItemList) throws LMISException{
        dbUtil.withDaoAsBatch(RnrFormItem.class, new DbUtil.Operation<RnrFormItem, Void>() {
            @Override
            public Void operate(Dao<RnrFormItem, String> dao) throws SQLException {
                for (RnrFormItem item : rnrFormItemList) {
                    dao.create(item);
                }
                return null;
            }
        });
    }

    public void createRegimenItems(final List<RegimenItem> regimenItemList) throws LMISException {
        dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                for (RegimenItem item : regimenItemList) {
                    dao.create(item);
                }
                return null;
            }
        });
    }

    public void createBaseInfoItems(final List<BaseInfoItem> baseInfoItemList) throws LMISException {
        dbUtil.withDaoAsBatch(BaseInfoItem.class, new DbUtil.Operation<BaseInfoItem, Void>() {
            @Override
            public Void operate(Dao<BaseInfoItem, String> dao) throws SQLException {
                for (BaseInfoItem item : baseInfoItemList){
                    dao.create(item);
                }
                return null;
            }
        });
    }
}
