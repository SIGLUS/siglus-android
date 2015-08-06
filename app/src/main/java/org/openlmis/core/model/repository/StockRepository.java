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
import android.util.Log;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockRepository {
    @Inject
    DbUtil dbUtil;

    GenericDao<StockCard> genericDao;
    GenericDao<StockItem> stockItemGenericDao;

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class,context);
        stockItemGenericDao = new GenericDao<>(StockItem.class, context);
    }


    public void batchSave(final List<StockCard> stockCards) {
        try {
            dbUtil.withDaoAsBatch(StockCard.class, new DbUtil.Operation<StockCard, Object>() {
                @Override
                public Object operate(Dao<StockCard, String> dao) throws SQLException {
                    for (StockCard stockCard : stockCards) {
                        dao.createOrUpdate(stockCard);
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }


    public void save(final StockCard stockCard) {
        try {
            genericDao.create(stockCard);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public void saveStockItems(final ArrayList<StockItem> stockItems) throws LMISException{
        dbUtil.withDaoAsBatch(StockItem.class, new DbUtil.Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {

                for (StockItem item : stockItems){
                    dao.create(item);
                }
                return null;
            }
        });
    }

    public List<StockCard> list() throws LMISException{
        return genericDao.queryForAll();
    }

    public List<StockCard> list(String programCode) throws LMISException{
        //TODO
        return new ArrayList<>();
    }

    public List<StockItem> listStockItems() throws LMISException {
        return stockItemGenericDao.queryForAll();
    }

    public List<StockItem> queryStockItems(final StockCard stockCard, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, List<StockItem>>() {
            @Override
            public List<StockItem> operate(Dao<StockItem, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("stockCard_id", stockCard.getId()).and().ge("createdAt", startDate).and().le("createdAt", endDate).query();
            }
        });
    }

    public long sum(final StockItem.MovementType movementType, final StockCard stockCard, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, Long>() {
            @Override
            public Long operate(Dao<StockItem, String> dao) throws SQLException {

                String query = "select sum(amount) from stock_items where stockCard_id=" + stockCard.getId()
                        + " and movementType='" + movementType
                        + "' and createdAt<='" + DateUtil.formatDate(endDate) + "' and createdAt>='" + DateUtil.formatDate(startDate) + "'";

                Log.d("StockRepository", query);
                return dao.queryRawValue(query);
            }
        });
    }

}
