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
import android.util.Log;

import com.google.inject.Inject;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.LotMovementItem;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockRepository {
    private static final String TAG = StockRepository.class.getSimpleName();
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
    @Inject
    CmmRepository cmmRepository;

    GenericDao<StockCard> genericDao;


    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class, context);
    }

    public void batchSaveUnpackStockCardsWithMovementItemsAndUpdateProduct(final List<StockCard> stockCards) {
        try {
            dbUtil.withDaoAsBatch(StockCard.class, dao -> {
                for (StockCard stockCard : stockCards) {
                    dao.createOrUpdate(stockCard);
                    updateProductOfStockCard(stockCard.getProduct());
                    stockMovementRepository.batchCreateOrUpdateStockMovementsAndLotInfo(stockCard.getStockMovementItemsWrapper());
                }
                return null;
            });
        } catch (LMISException e) {
            new LMISException(e, "StockRepository.batchSave").reportToFabric();
        }
    }

    public void createOrUpdate(final StockCard stockCard) {
        try {
            StockCard existStockCard = queryStockCardByProductId(stockCard.getProduct().getId());
            if (existStockCard != null) {
                stockCard.setId(existStockCard.getId());
            }
            genericDao.createOrUpdate(stockCard);
        } catch (LMISException e) {
            new LMISException(e, "StockRepository.createOrUpdate").reportToFabric();
        }
    }

    public void refresh(StockCard stockCard) {
        try {
            genericDao.refresh(stockCard);
        } catch (LMISException e) {
            new LMISException(e, "StockRepository.refresh").reportToFabric();
        }
    }

    protected void saveStockCardAndBatchUpdateMovements(final StockCard stockCard) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
                createOrUpdate(stockCard);
                lotRepository.createOrUpdateLotsInformation(stockCard.getLotOnHandListWrapper());
                stockMovementRepository.batchCreateOrUpdateStockMovementsAndLotMovements(stockCard.getStockMovementItemsWrapper());
                return null;
            });
        } catch (SQLException e) {
            new LMISException(e, "StockRepository.saveStock").reportToFabric();
        }
    }

    public void updateProductOfStockCard(Product product) {
        try {
            productRepository.updateProduct(product);
        } catch (LMISException e) {
            new LMISException(e, "StockRepository.updateP").reportToFabric();
        }
    }

    public void createOrUpdateStockCardWithStockMovement(final StockCard stockCard) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
                createOrUpdate(stockCard);
                stockMovementRepository.batchCreateStockMovementItemAndLotItems(stockCard.generateInitialStockMovementItem());
                updateProductOfStockCard(stockCard.getProduct());
                return null;
            });
        } catch (SQLException e) {
            new LMISException(e, "StockRepository.addStock").reportToFabric();
        }
    }

    public synchronized void addStockMovementAndUpdateStockCard(final StockMovementItem stockMovementItem) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
                StockCard stockcard = stockMovementItem.getStockCard();
                createOrUpdate(stockcard);
                stockMovementRepository.batchCreateStockMovementItemAndLotItems(stockMovementItem);
                return null;
            });
        } catch (SQLException e) {
            new LMISException(e, "StockRepository.addStock").reportToFabric();
        }

    }

    public List<StockCard> list() {
        try {
            List<StockCard> stockCards = genericDao.queryForAll();
            Collections.sort(stockCards);
            return stockCards;
        } catch (LMISException e) {
            new LMISException(e, "StockRepository:list").reportToFabric();
        }
        return null;
    }

    public boolean hasStockData() {
        List<StockCard> list = list();
        return list != null && list.size() > 0;
    }

    private boolean hasStockCardData(List<StockCard> list) {
        return list != null && list.size() > 0;
    }

    public boolean hasOldDate() {
        Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(DateUtil.getCurrentDate(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());

        List<StockCard> list = list();
        if (hasStockCardData(list)) {
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

    public List<StockCard> listStockCardsByProductIds(final List<Long> productIds) throws LMISException {
        return dbUtil.withDao(StockCard.class, dao -> dao.queryBuilder().where().in("product_id", productIds).query());
    }

    public List<StockCard> listEmergencyStockCards() throws LMISException {
        List<Program> programs = programRepository.listEmergencyPrograms();

        List<String> programCodes = from(programs).transform(program -> program.getProgramCode()).toList();
        List<Long> productIds = productProgramRepository.queryActiveProductIdsByProgramsWithKits(programCodes, false);
        return listStockCardsByProductIds(productIds);
    }

    public StockCard queryStockCardById(final long id) throws LMISException {
        return genericDao.getById(String.valueOf(id));
    }

    public StockCard queryStockCardByProductId(final long productId) throws LMISException {
        return dbUtil.withDao(StockCard.class, dao -> dao.queryBuilder().where().eq("product_id", productId).queryForFirst());
    }

    public void updateStockCardWithProduct(final StockCard stockCard) throws LMISException {
        dbUtil.withDaoAsBatch(StockCard.class, dao -> {
            dao.update(stockCard);
            updateProductOfStockCard(stockCard.getProduct());
            return null;
        });
    }

    protected List<StockCard> getStockCardsBeforePeriodEnd(RnRForm rnRForm) throws LMISException {
        return getStockCardsBeforePeriodEnd(rnRForm.getProgram().getProgramCode(), rnRForm.getPeriodEnd());
    }

    public List<StockCard> getStockCardsBelongToProgram(String programCode) throws LMISException {
        String rawSql = "SELECT * FROM stock_cards WHERE product_id IN ("
                + " SELECT id FROM products WHERE isActive =1 AND isArchived = 0 AND code IN ("
                + " SELECT productCode FROM product_programs WHERE isActive=1 AND programCode IN ("
                + " SELECT programCode FROM programs WHERE parentCode= '" + programCode + "'"
                + " OR programCode='" + programCode + "')))";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<StockCard> stockCardList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                StockCard stockCard = new StockCard();
                stockCard.setProduct(productRepository.getProductById(cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))));
                stockCard.setStockOnHand(cursor.getLong(cursor.getColumnIndexOrThrow("stockOnHand")));
                stockCard.setAvgMonthlyConsumption(cursor.getFloat(cursor.getColumnIndexOrThrow("avgMonthlyConsumption")));
                stockCard.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                stockCardList.add(stockCard);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return stockCardList;
    }

    protected List<StockCard> getStockCardsBeforePeriodEnd(String programCode, Date periodEnd) throws LMISException {
        String rawSql = "SELECT * FROM stock_cards WHERE product_id IN ("
                + " SELECT id FROM products WHERE isActive =1 AND isArchived = 0 AND code IN ("
                + " SELECT productCode FROM product_programs WHERE isActive=1 AND programCode IN ("
                + " SELECT programCode FROM programs WHERE parentCode= '" + programCode + "'"
                + " OR programCode='" + programCode + "')))"
                + " AND id NOT IN ("
                + " SELECT stockCard_id FROM stock_items WHERE stockCard_id NOT IN ("
                + " SELECT stockCard_id FROM stock_items"
                + " WHERE movementDate <= '" + DateUtil.formatDateTime(periodEnd) + "'"
                + " AND createdTime <= '" + DateUtil.formatDateTime(periodEnd) + "'))";

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
        return dbUtil.withDao(LotOnHand.class, dao -> dao.queryBuilder()
                .where()
                .eq("stockCard_id", stockCardId)
                .query());
    }

    public void batchCreateSyncDownStockCardsAndMovements(final List<StockCard> stockCards) throws SQLException {
        TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
            for (StockCard stockCard : stockCards) {
                if (stockCard.getId() <= 0) {
                    saveStockCardAndBatchUpdateMovements(stockCard);
                } else {
                    stockMovementRepository.batchCreateOrUpdateStockMovementsAndLotMovements(stockCard.getStockMovementItemsWrapper());
                }
            }
            return null;
        });
    }

    public void deleteOldData() {
        String dueDateShouldDataLivedInDB = DateUtil.formatDate(DateUtil.dateMinusMonth(DateUtil.getCurrentDate(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData()), DateUtil.DB_DATE_FORMAT);

        String rawSqlDeleteLotItems = "DELETE FROM lot_movement_items "
                + "WHERE StockMovementItem_id IN (SELECT id FROM stock_items WHERE movementDate < '" + dueDateShouldDataLivedInDB + "' );";
        String rawSqlDeleteStockMovementItems = "DELETE FROM stock_items "
                + "WHERE movementDate < '" + dueDateShouldDataLivedInDB + "'; ";

        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteLotItems);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteStockMovementItems);
    }

    public void deletedData(StockCard stockCard) throws LMISException {
        stockMovementRepository.deleteStockMovementItems(stockCard);
        cmmRepository.deleteCmm(stockCard);
        genericDao.delete(stockCard);
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

    public void deleteStockMovementsForDirtyData(List<String> productCodeList) {

        for (String productCode : productCodeList) {
            String deleteLotMovementItems = "DELETE FROM lot_movement_items "
                    + "WHERE lot_id=(SELECT id FROM lots WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "' ));";
            String deleteStockItems = "DELETE FROM stock_items "
                    + "WHERE stockCard_id=(SELECT id FROM stock_cards WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "' ));";
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteLotMovementItems);
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteStockItems);
        }
    }

    public void resetStockCard(List<String> productCodeList) {

        for (String productCode : productCodeList) {
            String resetStockCardSohAndAvgMonthlyConsumption = "UPDATE stock_cards SET stockOnHand=0,avgMonthlyConsumption=-1.0 "
                    + "WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "' );";
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(resetStockCardSohAndAvgMonthlyConsumption);
        }
    }

    public void insertNewInventory(List<String> productCodeList) throws LMISException {
        Date now = DateUtil.getCurrentDate();
        Cursor getStockCardCursor;

        for (String productCode : productCodeList) {
            String getStockCard = "SELECT * FROM stock_cards WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "');";
            getStockCardCursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(getStockCard, null);
            if (getStockCardCursor != null && getStockCardCursor.moveToFirst()) {
                StockMovementItem addNewStockMovementItem = new StockMovementItem();
                StockCard stockCard = getStockCardById(getStockCardCursor.getInt(getStockCardCursor.getColumnIndexOrThrow("id"))).get(0);
                addNewStockMovementItem.setCreatedTime(now);
                addNewStockMovementItem.setStockOnHand(0);
                addNewStockMovementItem.setDocumentNumber(null);
                addNewStockMovementItem.setMovementDate(now);
                addNewStockMovementItem.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
                addNewStockMovementItem.setReason("INVENTORY");
                addNewStockMovementItem.setRequested(null);
                addNewStockMovementItem.setMovementQuantity(0);
                addNewStockMovementItem.setSignature(null);
                addNewStockMovementItem.setSynced(false);
                addNewStockMovementItem.setCreatedAt(now);
                addNewStockMovementItem.setUpdatedAt(now);
                addNewStockMovementItem.setStockCard(stockCard);
                dbUtil.withDao(StockMovementItem.class, (DbUtil.Operation<StockMovementItem, Void>) dao -> {
                    dao.createOrUpdate(addNewStockMovementItem);
                    return null;
                });
            }
        }
    }

    private List<StockCard> getStockCardById(int stockCardId) throws LMISException {
        return dbUtil.withDao(StockCard.class, dao -> dao.queryBuilder().where().eq("id", stockCardId).query());
    }

    public void resetLotsOnHand(List<String> productCodeList) {
        Cursor getLotsOnHandItemsCursor = null;
        for (String productCode : productCodeList) {
            String getLotsOnHandItemsByStockCardId = "SELECT * FROM lots_on_hand "
                    + "WHERE stockCard_id=(SELECT id FROM stock_cards WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "'));";
            getLotsOnHandItemsCursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(getLotsOnHandItemsByStockCardId, null);
            while (getLotsOnHandItemsCursor.moveToNext()) {
                int lotsOnHandId = getLotsOnHandItemsCursor.getInt(getLotsOnHandItemsCursor.getColumnIndexOrThrow("id"));
                String reSetQuantityOnHandValue = "UPDATE lots_on_hand "
                        + "SET quantityOnHand=0 WHERE id='" + lotsOnHandId + "';";
                LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(reSetQuantityOnHandValue);
            }
        }
        if (!getLotsOnHandItemsCursor.isClosed()) {
            getLotsOnHandItemsCursor.close();
        }
    }

    public List<String> cardIdsIfLotOnHandLessZero() {
        List<String> cardIds = new ArrayList<>();
        try {
            GenericRawResults<String[]> stockCardIds = dbUtil.withDao(LotOnHand.class, dao -> (dao.queryRaw("select distinct stockCard_id from lots_on_hand where quantityOnHand < 0")));
            for (String[] resultArray : stockCardIds) {
                cardIds.add(resultArray[0]);
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }

        return cardIds;
    }

    public Map<String, String> lotOnHands() {
        Map<String, String> lotsOnHands = new HashMap<>();
        try {
            GenericRawResults<String[]> rawResults = dbUtil.withDao(LotOnHand.class, dao -> (dao.queryRaw("select stockCard_id, sum(quantityOnHand) from lots_on_hand group by stockCard_id")));
            for (String[] resultArray : rawResults) {
                lotsOnHands.put(resultArray[0], resultArray[1]);
            }
            rawResults.close();
            Log.d("lotOnHands", rawResults.toString());
        } catch (LMISException | SQLException e) {
            new LMISException(e, "StockRepository.getLotOnHands").reportToFabric();
        }
        return lotsOnHands;
    }

    public GenericRawResults<String[]> refreshedLotOnHands(Long stockCardId) throws LMISException {
        String querySql = "select stockCard_id, sum(quantityOnHand) from lots_on_hand where stockCard_id = " + stockCardId;
        return dbUtil.withDao(LotOnHand.class, dao -> dao.queryRaw(querySql));
    }

    public List<StockCard> queryCheckedStockCards(Set<String> filterStockCardIds) {
        String querySql = "select * from stock_cards where id NOT IN ("
                + StringUtils.join(filterStockCardIds, ",")
                + ")";
        List<StockCard> checkedStockCards = new ArrayList<>();

        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(querySql, null);
        if (cursor.moveToFirst()) {
            do {
                StockCard stockCard = new StockCard();
                stockCard.setStockOnHand(cursor.getLong(cursor.getColumnIndexOrThrow("stockOnHand")));
                stockCard.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                checkedStockCards.add(stockCard);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return checkedStockCards;
    }

    public void resetKeepLotsOnHand(List<LotMovementItem> lotMovementItems, Map<String, List<StockMovementItem>> stockMovementItemsMap) {
        try {
            Map<String, LotMovementItem> idToLotMovements = new HashMap<>();
            for(LotMovementItem item : lotMovementItems) {
                idToLotMovements.put(String.valueOf(item.getLot().getId()), item);
            }

            List<LotOnHand> lotOnHands = getLotOnHandByStockCards(stockMovementItemsMap.keySet());
            for (LotOnHand lotOnHand : lotOnHands) {
                if (idToLotMovements.containsKey(String.valueOf(lotOnHand.getLot().getId()))) {
                    LotMovementItem lotMovementItem = idToLotMovements.get(String.valueOf(lotOnHand.getLot().getId()));
                    lotOnHand.setQuantityOnHand(lotMovementItem.getStockOnHand());
                } else {
                    lotOnHand.setQuantityOnHand((long) 0);
                }
            }
            dbUtil.withDaoAsBatch(LotOnHand.class, (DbUtil.Operation<LotOnHand, Void>) dao -> {
                for (LotOnHand lotOnHand : lotOnHands) {
                    dao.createOrUpdate(lotOnHand);
                }
                return null;
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }

    }

    private List<LotOnHand> getLotOnHandByStockCards(final Set<String> stockCardIds) throws LMISException {
        return dbUtil.withDao(LotOnHand.class, dao -> dao.queryBuilder()
                .where()
                .in("stockCard_id", stockCardIds)
                .query());
    }

}