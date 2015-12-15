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
import com.j256.ormlite.table.TableUtils;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
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
import java.util.Collections;
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
    GenericDao<DraftInventory> draftInventoryGenericDao;
    GenericDao<Product> productGenericDao;


    @Inject
    ProgramRepository programRepository;

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class, context);
        stockItemGenericDao = new GenericDao<>(StockMovementItem.class, context);
        draftInventoryGenericDao = new GenericDao<>(DraftInventory.class, context);
        productGenericDao = new GenericDao<>(Product.class, context);
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
            e.reportToFabric();
        }
    }

    public void save(final StockCard stockCard) {
        try {
            genericDao.create(stockCard);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void update(final StockCard stockCard) {
        try {
            genericDao.update(stockCard);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void refresh(StockCard stockCard) {
        try {
            genericDao.refresh(stockCard);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void batchCreateOrUpdateStockMovements(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException {
                for (StockMovementItem stockMovementItem : stockMovementItems) {
                    UpdateDateTimeIfEmpty(stockMovementItem);
                    dao.createOrUpdate(stockMovementItem);
                }
                return null;
            }
        });
    }

    public void batchUpdateStockMovements(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException {
                for (StockMovementItem stockMovementItem : stockMovementItems) {
                    UpdateDateTimeIfEmpty(stockMovementItem);
                    dao.update(stockMovementItem);
                }
                return null;
            }
        });
    }

    private void UpdateDateTimeIfEmpty(StockMovementItem stockMovementItem) {
        if (stockMovementItem.getCreatedTime() == null) {
            stockMovementItem.setCreatedTime(new Date());
        }
        stockMovementItem.setCreatedAt(new Date());
        stockMovementItem.setUpdatedAt(new Date());
    }

    public void saveStockCardAndBatchUpdateMovements(final StockCard stockCard) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    save(stockCard);
                    batchCreateOrUpdateStockMovements(stockCard.getStockMovementItemsWrapper());
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }

    }

    public void saveStockItem(final StockMovementItem stockMovementItem) throws LMISException {
        stockMovementItem.setCreatedTime(new Date());
        stockItemGenericDao.create(stockMovementItem);
    }

    public void updateProductOfStockCard(final StockCard stockCard) {
        try {
            productGenericDao.update(stockCard.getProduct());
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void initStockCard(final StockCard stockCard) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    save(stockCard);
                    addStockMovementAndUpdateStockCard(stockCard, initStockMovementItem(stockCard));
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void reInventoryArchivedStockCard(final StockCard stockCard) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    update(stockCard);
                    updateProductOfStockCard(stockCard);
                    saveStockItem(initStockMovementItem(stockCard));
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    protected StockMovementItem initStockMovementItem(StockCard stockCard) {
        StockMovementItem initInventory = new StockMovementItem();
        initInventory.setReason(MovementReasonManager.INVENTORY);
        initInventory.setMovementType(StockMovementItem.MovementType.PHYSICAL_INVENTORY);
        initInventory.setMovementDate(new Date());
        initInventory.setMovementQuantity(stockCard.getStockOnHand());
        initInventory.setStockOnHand(stockCard.getStockOnHand());
        initInventory.setStockCard(stockCard);
        initInventory.setCreatedTime(new Date());
        return initInventory;
    }

    public void addStockMovementAndUpdateStockCard(StockCard stockcard, StockMovementItem stockMovementItem) throws LMISException {
        if (stockcard == null) {
            return;
        }

        update(stockcard);
        saveStockItem(stockMovementItem);
    }

    public List<StockMovementItem> listUnSynced() throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("synced", false).query();
            }
        });
    }

    public List<StockCard> list() throws LMISException {
        List<StockCard> stockCards = genericDao.queryForAll();
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.sort_product_list_alphabetically_435)) {
            Collections.sort(stockCards);
        }
        return stockCards;
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
                return Lists.reverse(dao.queryBuilder().limit(5L).orderBy("movementDate", false).orderBy("createdTime", false).orderBy("id", false).where().eq("stockCard_id", stockCardId).query());
            }
        });
    }

    public List<StockMovementItem> queryStockItems(final StockCard stockCard, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .orderBy("movementDate", false)
                        .orderBy("createdTime", false)
                        .where()
                        .eq("stockCard_id", stockCard.getId())
                        .and().ge("movementDate", startDate)
                        .and().le("movementDate", endDate)
                        .query();
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

    public StockCard queryStockCardByProductId(final long productId) throws LMISException {
        return dbUtil.withDao(StockCard.class, new DbUtil.Operation<StockCard, StockCard>() {
            @Override
            public StockCard operate(Dao<StockCard, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("product_id", productId).queryForFirst();
            }
        });
    }

    public List<StockMovementItem> queryStockItemsHistory(final long stockCardId, final long startIndex, final long maxRows) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return Lists.reverse(dao.queryBuilder().offset(startIndex).limit(maxRows).orderBy("movementDate", false).orderBy("createdTime", false).orderBy("id", false).where().eq("stockCard_id", stockCardId).query());
            }
        });
    }

    public void saveDraftInventory(DraftInventory draftInventory) throws LMISException {
        draftInventoryGenericDao.create(draftInventory);
    }

    public List<DraftInventory> listDraftInventory() throws LMISException {
        return draftInventoryGenericDao.queryForAll();
    }

    public void clearDraftInventory() throws LMISException {
        try {
            TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(), DraftInventory.class);
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }
}
