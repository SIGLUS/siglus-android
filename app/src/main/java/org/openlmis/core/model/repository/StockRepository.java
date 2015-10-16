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

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class StockRepository {
    @Inject
    DbUtil dbUtil;

    @Inject
    Context context;

    GenericDao<StockCard> genericDao;
    GenericDao<StockMovementItem> stockItemGenericDao;

    @Inject
    ProgramRepository programRepository;

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class, context);
        stockItemGenericDao = new GenericDao<>(StockMovementItem.class, context);
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

    public void update(final StockCard stockCard) {
        try {
            genericDao.update(stockCard);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public void refresh(StockCard stockCard) {
        try {
            genericDao.refresh(stockCard);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }


    protected void saveStockItem(final StockMovementItem stockMovementItem) throws LMISException {
        dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException {
                dao.create(stockMovementItem);
                return null;
            }
        });
    }

    public void initStockCard(final StockCard stockCard) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    StockMovementItem initInventory = new StockMovementItem();
                    initInventory.setReason(context.getString(R.string.title_physical_inventory));
                    initInventory.setMovementType(StockMovementItem.MovementType.PHYSICAL_INVENTORY);
                    initInventory.setMovementDate(new Date());
                    initInventory.setMovementQuantity(0);

                    save(stockCard);
                    addStockMovementAndUpdateStockCard(stockCard, initInventory);
                    return null;
                }
            });
        }catch (SQLException e){
            e.printStackTrace();
            throw new LMISException(e);
        }
    }

    public void addStockMovementAndUpdateStockCard(StockCard stockcard, StockMovementItem stockMovementItem) throws LMISException {
        if (stockcard == null) {
            return;
        }

        if (stockMovementItem.getStockOnHand() == -1) {
            long stockExistence = stockcard.getStockOnHand();
            if (stockMovementItem.getMovementType() == StockMovementItem.MovementType.ISSUE
                    || stockMovementItem.getMovementType() == StockMovementItem.MovementType.NEGATIVE_ADJUST) {
                stockExistence -= stockMovementItem.getMovementQuantity();
            } else {
                stockExistence += stockMovementItem.getMovementQuantity();
            }
            stockMovementItem.setStockOnHand(stockExistence);
        }

        stockcard.setStockOnHand(stockMovementItem.getStockOnHand());
        update(stockcard);
        stockMovementItem.setStockCard(stockcard);
        saveStockItem(stockMovementItem);
    }

    public List<StockCard> list() throws LMISException {
        return genericDao.queryForAll();
    }

    public List<StockCard> list(String programCode) throws LMISException {
        List<StockCard> stockCards = new ArrayList<>();
        final Program program = programRepository.queryByCode(programCode);
        if (program != null) {

            stockCards = FluentIterable.from(genericDao.queryForAll()).filter(new Predicate<StockCard>() {
                @Override
                public boolean apply(StockCard stockCard) {
                    return stockCard.getProduct().getProgram().getId() == program.getId();
                }
            }).toList();
        }
        return stockCards;
    }

    public List<StockMovementItem> listLastFive(final long stockCardId) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return Lists.reverse(dao.queryBuilder().limit(5L).orderBy("id", false).where().eq("stockCard_id", stockCardId).query());
            }
        });
    }


    public List<StockMovementItem> queryStockItems(final StockCard stockCard, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("stockCard_id", stockCard.getId()).and().ge("movementDate", startDate).and().le("movementDate", endDate).query();
            }
        });
    }

    public StockCard queryStockCardById(final long id) throws LMISException {
        return dbUtil.withDao(StockCard.class, new DbUtil.Operation<StockCard, StockCard>() {
            @Override
            public StockCard operate(Dao<StockCard, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("id", id).queryForFirst();
            }
        });
    }


    public List<StockMovementItem> queryStockItemsHistory(final long stockCardId, final long startIndex, final long maxRows) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return Lists.reverse(dao.queryBuilder().offset(startIndex).limit(maxRows).orderBy("id", false).where().eq("stockCard_id", stockCardId).query());
            }
        });
    }

}
