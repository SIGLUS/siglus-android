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
import android.database.Cursor;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.StockMovementIsNullException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockRepository {
    @Inject
    DbUtil dbUtil;
    @Inject
    Context context;
    @Inject
    ProductRepository productRepository;
    @Inject
    ProgramRepository programRepository;
    @Inject
    ProductProgramRepository productProgramRepository;
    @Inject
    LotRepository lotRepository;
    @Inject
    InventoryRepository inventoryRepository;

    GenericDao<StockCard> genericDao;
    GenericDao<StockMovementItem> stockItemGenericDao;
    GenericDao<DraftInventory> draftInventoryGenericDao;

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class, context);
        stockItemGenericDao = new GenericDao<>(StockMovementItem.class, context);
        draftInventoryGenericDao = new GenericDao<>(DraftInventory.class, context);
    }

    public void batchSaveUnpackStockCardsWithMovementItemsAndUpdateProduct(final List<StockCard> stockCards) {
        try {
            dbUtil.withDaoAsBatch(StockCard.class, new DbUtil.Operation<StockCard, Object>() {
                @Override
                public Object operate(Dao<StockCard, String> dao) throws SQLException, LMISException {
                    for (StockCard stockCard : stockCards) {
                        dao.createOrUpdate(stockCard);
                        updateProductOfStockCard(stockCard.getProduct());
                        batchCreateOrUpdateStockMovementsAndLotInfo(stockCard.getStockMovementItemsWrapper());
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void createOrUpdate(final StockCard stockCard) {
        try {
            genericDao.createOrUpdate(stockCard);
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

    public void batchCreateOrUpdateStockMovementsAndLotInfo(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException, LMISException {
                for (StockMovementItem stockMovementItem : stockMovementItems) {
                    updateDateTimeIfEmpty(stockMovementItem);
                    dao.createOrUpdate(stockMovementItem);
                    lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getLotMovementItemListWrapper());
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

    public void saveStockCardAndBatchUpdateMovements(final StockCard stockCard) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    createOrUpdate(stockCard);
                    lotRepository.createOrUpdateLotsInformation(stockCard.getLotOnHandListWrapper());
                    batchCreateOrUpdateStockMovementsAndLotMovements(stockCard.getStockMovementItemsWrapper());
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e).reportToFabric();
        }
    }

    public void saveStockItem(final StockMovementItem stockMovementItem) throws LMISException {
        stockMovementItem.setCreatedTime(new Date(LMISApp.getInstance().getCurrentTimeMillis()));
        stockItemGenericDao.create(stockMovementItem);

        lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getLotMovementItemListWrapper());
        lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getNewAddedLotMovementItemListWrapper());
    }

    public void updateProductOfStockCard(Product product) {
        try {
            productRepository.updateProduct(product);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void createOrUpdateStockCardWithStockMovement(final StockCard stockCard) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    createOrUpdate(stockCard);
                    updateProductOfStockCard(stockCard.getProduct());
                    saveStockItem(stockCard.generateInitialStockMovementItem());
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e).reportToFabric();
        }
    }

    public void addStockMovementAndUpdateStockCard(final StockMovementItem stockMovementItem) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    StockCard stockcard = stockMovementItem.getStockCard();
                    createOrUpdate(stockcard);
                    saveStockItem(stockMovementItem);
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e).reportToFabric();
        }

    }

    public List<StockMovementItem> listUnSynced() throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("synced", false).query();
            }
        });
    }

    public List<StockCard> list() {
        try {
            List<StockCard> stockCards = genericDao.queryForAll();
            Collections.sort(stockCards);
            return stockCards;
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return null;
    }

    public boolean hasStockData() {
        List<StockCard> list = list();
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean hasOldDate() {
        Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(new Date(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());

        List<StockCard> list = list();
        if (hasStockData()) {
            for (StockCard stockCard : list) {
              for(StockMovementItem stockMovementItem: stockCard.getStockMovementItemsWrapper()){
                  if (stockMovementItem.getMovementDate().before(dueDateShouldDataLivedInDB)) {
                      return true;
                  }
              }
            }
        }
        return false;
    }

    private List<StockCard> listStockCardsByProductIds(final List<Long> productIds) throws LMISException {
        return dbUtil.withDao(StockCard.class, new DbUtil.Operation<StockCard, List<StockCard>>() {
            @Override
            public List<StockCard> operate(Dao<StockCard, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().in("product_id", productIds).query();
            }
        });
    }


    public List<StockCard> listEmergencyStockCards() throws LMISException {
        List<Program> programs = programRepository.listEmergencyPrograms();

        List<String> programCodes = from(programs).transform(new Function<Program, String>() {
            @Override
            public String apply(Program program) {
                return program.getProgramCode();
            }
        }).toList();
        List<Long> productIds = productProgramRepository.queryActiveProductIdsByProgramsWithKits(programCodes, false);
        return listStockCardsByProductIds(productIds);
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
                        .and().gt("createdTime", periodBeginDate)//difference from the api above
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

    public StockMovementItem queryFirstStockMovementItem(final StockCard stockCard) throws LMISException {
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


    public void updateStockCardWithProduct(final StockCard stockCard) throws LMISException {
        dbUtil.withDaoAsBatch(StockCard.class, new DbUtil.Operation<StockCard, Object>() {
            @Override
            public Object operate(Dao<StockCard, String> dao) throws SQLException, LMISException {
                dao.update(stockCard);
                updateProductOfStockCard(stockCard.getProduct());
                return null;
            }
        });

    }

    public Date queryEarliestStockMovementDateByProgram(final String programCode) {
        Date earliestDate = null;

        String rawSql = "SELECT movementDate FROM stock_items s1 "
                + "JOIN stock_cards s2 ON s1.stockCard_id = s2.id "
                + "JOIN products p1 ON s2.product_id = p1.id "
                + "JOIN product_programs p2 ON p2.productCode = p1.code "
                + "JOIN programs p3 ON p2.programCode = p3.programCode "
                + "WHERE p1.isActive = 1 AND p1.isArchived = 0 AND p2.isActive = 1 AND p3.programCode = '" + programCode + "' "
                + "OR p3.parentCode = '" + programCode + "'";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<String> movementDates = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                movementDates.add(cursor.getString(cursor.getColumnIndexOrThrow("movementDate")));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }

        for (String movementDate : movementDates) {
            Date date = DateUtil.parseString(movementDate, DateUtil.DB_DATE_FORMAT);
            if (earliestDate == null || date.before(earliestDate)) {
                earliestDate = date;
            }
        }
        return earliestDate;
    }

    public StockMovementItem getFirstStockMovement() throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, StockMovementItem>() {
            @Override
            public StockMovementItem operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .queryForFirst();
            }
        });
    }

    protected List<StockCard> getStockCardsBeforePeriodEnd(RnRForm rnRForm) throws LMISException {
        String rawSql = "SELECT * FROM stock_cards WHERE product_id IN ("
                + " SELECT id FROM products WHERE isActive =1 AND isArchived = 0 AND code IN ("
                + " SELECT productCode FROM product_programs WHERE isActive=1 AND programCode IN ("
                + " SELECT programCode FROM programs WHERE parentCode= '" + rnRForm.getProgram().getProgramCode() + "'"
                + " OR programCode='" + rnRForm.getProgram().getProgramCode() + "')))"
                + " AND id NOT IN ("
                + " SELECT stockCard_id FROM stock_items WHERE stockCard_id NOT IN ("
                + " SELECT stockCard_id FROM stock_items"
                + " WHERE movementDate <= '" + DateUtil.formatDateTime(rnRForm.getPeriodEnd()) + "'"
                + " AND createdTime <= '" + DateUtil.formatDateTime(rnRForm.getPeriodEnd()) + "'))";

        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<StockCard> stockCardList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                StockCard stockCard = new StockCard();
                stockCard.setExpireDates(cursor.getString(cursor.getColumnIndexOrThrow("expireDates")));
                stockCard.setProduct(productRepository.getProductById(cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))));
                stockCard.setStockOnHand(cursor.getLong(cursor.getColumnIndexOrThrow("stockOnHand")));
                stockCard.setAvgMonthlyConsumption(cursor.getFloat(cursor.getColumnIndexOrThrow("avgMonthlyConsumption")));
                stockCard.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                stockCard.setLotOnHandListWrapper(getLotOnHandByStockCard(stockCard.getId()));
                stockCardList.add(stockCard);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return stockCardList;
    }

    private List<LotOnHand> getLotOnHandByStockCard(final long stockCardId) throws LMISException {
        return dbUtil.withDao(LotOnHand.class, new DbUtil.Operation<LotOnHand, List<LotOnHand>>() {
            @Override
            public List<LotOnHand> operate(Dao<LotOnHand, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder()
                        .where()
                        .eq("stockCard_id", stockCardId)
                        .query();
            }
        });
    }

    public void batchCreateOrUpdateStockMovementsAndLotMovements(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException, LMISException {
                for (StockMovementItem stockMovementItem : stockMovementItems) {
                    updateDateTimeIfEmpty(stockMovementItem);
                    dao.createOrUpdate(stockMovementItem);
                    for (LotMovementItem lotMovementItem : stockMovementItem.getLotMovementItemListWrapper()) {
                        lotRepository.createLotMovementItem(lotMovementItem);
                    }
                }
                return null;
            }
        });
    }

    public void batchCreateSyncDownStockCardsAndMovements(final SyncDownStockCardResponse syncDownStockCardResponse) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    for (StockCard stockCard : syncDownStockCardResponse.getStockCards()) {
                        if (stockCard.getId() <= 0) {
                            saveStockCardAndBatchUpdateMovements(stockCard);
                        } else {
                            batchCreateOrUpdateStockMovementsAndLotMovements(stockCard.getStockMovementItemsWrapper());
                        }
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e).reportToFabric();
        }
    }

    public void deleteOldData() {
        String dueDateShouldDataLivedInDB = DateUtil.formatDate(DateUtil.dateMinusMonth(new Date(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData()), DateUtil.DB_DATE_FORMAT);

        String rawSqlDeleteLotItems = "DELETE FROM lot_movement_items "
                + "WHERE StockMovementItem_id IN (SELECT id FROM stock_items WHERE movementDate < '" + dueDateShouldDataLivedInDB + "' );";
        String rawSqlDeleteStockMovementItems = "DELETE FROM stock_items "
                + "WHERE movementDate < '" + dueDateShouldDataLivedInDB + "'; ";

        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteLotItems);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteStockMovementItems);
    }
}