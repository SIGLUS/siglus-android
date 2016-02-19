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
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.StockMovementIsNullException;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
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

    private final int LOW_STOCK_CALCULATE_MONTH_QUANTITY = 3;

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class, context);
        stockItemGenericDao = new GenericDao<>(StockMovementItem.class, context);
        draftInventoryGenericDao = new GenericDao<>(DraftInventory.class, context);
        productGenericDao = new GenericDao<>(Product.class, context);
    }

    public void batchSaveStockCardsWithMovementItemsAndUpdateProduct(final List<StockCard> stockCards) {
        try {
            dbUtil.withDaoAsBatch(StockCard.class, new DbUtil.Operation<StockCard, Object>() {
                @Override
                public Object operate(Dao<StockCard, String> dao) throws SQLException, LMISException {
                    for (StockCard stockCard : stockCards) {
                        dao.createOrUpdate(stockCard);
                        updateProductOfStockCard(stockCard);
                        batchCreateOrUpdateStockMovements(stockCard.getStockMovementItemsWrapper());
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
                    updateDateTimeIfEmpty(stockMovementItem);
                    dao.createOrUpdate(stockMovementItem);
                }
                return null;
            }
        });
    }

    private void updateDateTimeIfEmpty(StockMovementItem stockMovementItem) {
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
        stockMovementItem.setCreatedTime(new Date(LMISApp.getInstance().getCurrentTimeMillis()));
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
                    addStockMovementAndUpdateStockCard(stockCard.generateInitialStockMovementItem());
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
                    saveStockItem(stockCard.generateInitialStockMovementItem());
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void addStockMovementAndUpdateStockCard(StockMovementItem stockMovementItem) throws LMISException {
        StockCard stockcard = stockMovementItem.getStockCard();
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
        Collections.sort(stockCards);
        return stockCards;
    }

    public boolean hasStockData() {
        try {
            List<StockCard> list = list();
            if (list != null && list.size() > 0) {
                return true;
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<StockCard> listActiveStockCardsByProgramId(final long programId) throws LMISException {
        return dbUtil.withDao(StockCard.class, new DbUtil.Operation<StockCard, List<StockCard>>() {
            @Override
            public List<StockCard> operate(Dao<StockCard, String> dao) throws SQLException {

                QueryBuilder<Product, String> productQueryBuilder = DbUtil.initialiseDao(Product.class).queryBuilder();
                productQueryBuilder.where().eq("program_id", programId).and().eq("isActive", true).and().eq("isKit", false);

                return dao.queryBuilder().join(productQueryBuilder)
                        .query();
            }
        });
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
                        .orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .where()
                        .eq("stockCard_id", stockCard.getId())
                        .and().ge("movementDate", startDate)
                        .and().le("movementDate", endDate)
                        .query();
            }
        });
    }

    public List<StockMovementItem> queryStockItemsByPeriodDates(final StockCard stockCard, final Date periodBeginDate, final Date periodEndDate) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .where()
                        .eq("stockCard_id", stockCard.getId())
                        .and().gt("createdTime", periodBeginDate)
                        .and().le("createdTime", periodEndDate)
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

    protected StockMovementItem queryFirstStockMovementItem(final StockCard stockCard) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, StockMovementItem>() {
            @Override
            public StockMovementItem operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .where()
                        .eq("stockCard_id", stockCard.getId())
                        .queryForFirst();
            }
        });
    }

    protected Date queryFirstPeriodBegin(final StockCard stockCard) throws LMISException {
        StockMovementItem stockMovementItem = queryFirstStockMovementItem(stockCard);
        if (stockMovementItem == null) {
            throw new StockMovementIsNullException(stockCard);
        }
        return stockMovementItem.getMovementPeriod().getBegin().toDate();
    }

    public int getLowStockAvg(StockCard stockCard) {
        return (int) Math.ceil(calculateAverageMonthlyConsumption(stockCard) * 0.05);
    }

    public int getCmm(StockCard stockCard) {
        return (int) Math.ceil(calculateAverageMonthlyConsumption(stockCard));
    }

    private float calculateAverageMonthlyConsumption(StockCard stockCard) {
        Date firstPeriodBegin;
        try {
            firstPeriodBegin = queryFirstPeriodBegin(stockCard);
        } catch (LMISException e) {
            e.reportToFabric();
            return 0;
        }

        List<Long> issuePerMonths = new ArrayList<>();
        Period period = Period.of(DateUtil.today());
        int periodQuantity = DateUtil.calculateDateMonthOffset(firstPeriodBegin, period.getBegin().toDate());

        if (periodQuantity < LOW_STOCK_CALCULATE_MONTH_QUANTITY) {
            return 0;
        }

        for (int i = 0; i < periodQuantity; i++) {
            period = period.previous();
            Long totalIssuesEachMonth = calculateTotalIssuesPerMonth(stockCard, period);

            if (totalIssuesEachMonth == null) {
                continue;
            }

            issuePerMonths.add(totalIssuesEachMonth);

            if (issuePerMonths.size() == LOW_STOCK_CALCULATE_MONTH_QUANTITY) {
                break;
            }
        }

        if (issuePerMonths.size() < LOW_STOCK_CALCULATE_MONTH_QUANTITY) {
            return 0;
        }
        return getTotalIssues(issuePerMonths) * 1f / LOW_STOCK_CALCULATE_MONTH_QUANTITY;
    }


    private long getTotalIssues(List<Long> issuePerMonths) {
        long total = 0;
        for (Long totalIssues : issuePerMonths) {
            total += totalIssues;
        }
        return total;
    }

    private Long calculateTotalIssuesPerMonth(StockCard stockCard, Period period) {
        long totalIssued = 0;
        List<StockMovementItem> stockMovementItems;
        try {
            stockMovementItems = queryStockItems(stockCard, period.getBegin().toDate(), period.getEnd().toDate());
        } catch (LMISException e) {
            e.reportToFabric();
            return null;
        }

        if (stockMovementItems.isEmpty()) {
            return 0L;
        }

        for (StockMovementItem item : stockMovementItems) {
            if (item.getStockOnHand() == 0) {
                return null;
            }
            if (StockMovementItem.MovementType.ISSUE == item.getMovementType()) {
                totalIssued += item.getMovementQuantity();
            }
        }
        return totalIssued;
    }
}
