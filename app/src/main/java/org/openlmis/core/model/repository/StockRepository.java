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
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;

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
    StockMovementRepository stockMovementRepository;

    GenericDao<StockCard> genericDao;

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class, context);
    }

    public void batchSaveUnpackStockCardsWithMovementItemsAndUpdateProduct(final List<StockCard> stockCards) {
        try {
            dbUtil.withDaoAsBatch(StockCard.class, new DbUtil.Operation<StockCard, Object>() {
                @Override
                public Object operate(Dao<StockCard, String> dao) throws SQLException, LMISException {
                    for (StockCard stockCard : stockCards) {
                        dao.createOrUpdate(stockCard);
                        updateProductOfStockCard(stockCard.getProduct());
                        stockMovementRepository.batchCreateOrUpdateStockMovementsAndLotInfo(stockCard.getStockMovementItemsWrapper());
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

    protected void saveStockCardAndBatchUpdateMovements(final StockCard stockCard) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    createOrUpdate(stockCard);
                    lotRepository.createOrUpdateLotsInformation(stockCard.getLotOnHandListWrapper());
                    stockMovementRepository.batchCreateOrUpdateStockMovementsAndLotMovements(stockCard.getStockMovementItemsWrapper());
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e).reportToFabric();
        }
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
                    stockMovementRepository.batchCreateStockMovementItemAndLotItems(stockCard.generateInitialStockMovementItem());
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
                    stockMovementRepository.batchCreateStockMovementItemAndLotItems(stockMovementItem);
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e).reportToFabric();
        }

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
        return list != null && list.size() > 0;
    }

    public boolean hasOldDate() {
        Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(new Date(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());

        List<StockCard> list = list();
        if (hasStockData()) {
            for (StockCard stockCard : list) {
                for (StockMovementItem stockMovementItem : stockCard.getStockMovementItemsWrapper()) {
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

    public StockCard queryStockCardById(final long id) throws LMISException {
        return genericDao.getById(String.valueOf(id));
    }

    public StockCard queryStockCardByProductId(final long productId) throws LMISException {
        return dbUtil.withDao(StockCard.class, new DbUtil.Operation<StockCard, StockCard>() {
            @Override
            public StockCard operate(Dao<StockCard, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("product_id", productId).queryForFirst();
            }
        });
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
                stockCard.setProduct(productRepository.getProductById(cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))));
                stockCard.setStockOnHand(cursor.getLong(cursor.getColumnIndexOrThrow("stockOnHand")));
                stockCard.setAvgMonthlyConsumption(cursor.getFloat(cursor.getColumnIndexOrThrow("avgMonthlyConsumption")));
                stockCard.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                stockCard.setLotOnHandListWrapper(getLotOnHandByStockCard(stockCard.getId()));
                stockCardList.add(stockCard);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
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

    public void batchCreateSyncDownStockCardsAndMovements(final List<StockCard> stockCards) throws SQLException {
        TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (StockCard stockCard : stockCards) {
                    if (stockCard.getId() <= 0) {
                        saveStockCardAndBatchUpdateMovements(stockCard);
                    } else {
                        stockMovementRepository.batchCreateOrUpdateStockMovementsAndLotMovements(stockCard.getStockMovementItemsWrapper());
                    }
                }
                return null;
            }
        });
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

    public StockCard queryStockCardByProductCode(String productCode) throws LMISException {
        String rawSql = "SELECT * FROM stock_cards WHERE product_id = ("
                + " SELECT id FROM products WHERE code = '" + productCode + "');";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);

        StockCard stockCard = null;

        if (cursor.moveToFirst()) {
            stockCard = new StockCard();
            stockCard.setProduct(productRepository.getProductById(cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))));
            stockCard.setStockOnHand(cursor.getLong(cursor.getColumnIndexOrThrow("stockOnHand")));
            stockCard.setAvgMonthlyConsumption(cursor.getFloat(cursor.getColumnIndexOrThrow("avgMonthlyConsumption")));
            stockCard.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            stockCard.setLotOnHandListWrapper(getLotOnHandByStockCard(stockCard.getId()));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return stockCard;
    }
}