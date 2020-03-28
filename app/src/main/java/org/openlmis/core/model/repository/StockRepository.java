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
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    createOrUpdate(stockCard);
                    stockMovementRepository.batchCreateStockMovementItemAndLotItems(stockCard.generateInitialStockMovementItem());
                    updateProductOfStockCard(stockCard.getProduct());
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e, "StockRepository.addStock").reportToFabric();
        }
    }

    public synchronized void addStockMovementAndUpdateStockCard(final StockMovementItem stockMovementItem) {
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
        Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(new Date(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());

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
        return getStockCardsBeforePeriodEnd(rnRForm.getProgram().getProgramCode(), rnRForm.getPeriodEnd());
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

    public void deletedData(Product product, boolean isFromKitToNormal) throws LMISException {
        Log.d(TAG, "deletedData, product = " + product);
        Log.d(TAG, "deletedData, isFromKitToNormal = " + isFromKitToNormal);
        StockCard stockCard = queryStockCardByProductCode(product.getCode());
        Product localProduct = productRepository.getByCode(product.getCode());
        Log.d(TAG, "deletedData, stockCard = " + stockCard);
        Log.d(TAG, "deletedData, local id = " + localProduct.getId() + ",remote id=" + product.getId());

        String rawSqlDeleteLotMovmentItem = "DELETE FROM lot_movement_items "
                + "where lot_id IN ( select id from lots where product_id=" + localProduct.getId() + ");";
        String rawSqlDeleteLotOnHand = "delete from lots_on_hand "
                + "where lot_id IN ( select id from lots where product_id=" + localProduct.getId() + ");";
        String rawSqlDeleteLots = "delete from lots where product_id=" + localProduct.getId() + ";";

        String rawSqlDeleteKitProducts = "delete from kit_products where kitCode=\"" + product.getCode() + "\";";

        String rawSqlDeleteProducts = "delete from products where code=\"" + product.getCode() + "\";";

        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteLotMovmentItem);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteLotOnHand);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteLots);
        if (stockCard == null && !isFromKitToNormal) {
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteProducts);
        }
        if (isFromKitToNormal) {
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteKitProducts);
        }
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

    public void deleteStockDirtyData(List<String> productCodeList){
        for (String productCode : productCodeList) {
            String deleteLotOnHand="DELETE FROM lot_on_hand "
                    + "WHERE lot_id=(SELECT id FROM lots WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "' ));";
            String deleteLotMovementItems="DELETE FROM lot_movement_items "
                    + "WHERE lot_id=(SELECT id FROM lots WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "' ));";
            String deleteStockItems="DELETE FROM stock_items "
                    + "WHERE stockCard_id=(SELECT id FROM stock_cards WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "' ));";
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteLotOnHand);
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteLotMovementItems);
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteStockItems);
        }
    }

    public void ResetStockCard(List<String> productCodeList){
        for (String productCode:productCodeList) {
            String resetStockCardSohAndAvgMonthlyConsumption="UPDATE stock_cards SET stockOnHand=0,avgMonthlyConsumption=-1.0 "+
                    "WHERE product_id=(SELECT id FROM products WHERE code='" + productCode + "' );";
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(resetStockCardSohAndAvgMonthlyConsumption);
        }
    }

    public void InsertANewInventory(List<String> productCodeList){
        Cursor stockCardId =null;
        for (String productCode:productCodeList) {
            Date newDate=new Date();
            String getStockCardId="SELECT id FROM stock_cards WHERE product_id=(SELECT id FROM products WHERE code=?);";
            String addNewInventory="INSERT INTO stock_items (id,documentNumber,movementDate,StockCard_id,MovementType,reason,movementQuantity,stockOnHand,createAt,updateAt,signature,synced,createTime,requested)"
                    +"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            stockCardId=LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(getStockCardId,new String[]{productCode});
            if(stockCardId!=null){
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(addNewInventory,
                    new Object[]{null,null,DateUtil.formatDate(newDate,DateUtil.DB_DATE_FORMAT),stockCardId.getInt(0),
                            "PHYSICAL_INVENTORY","INVENTORY",0,0,DateUtil.formatDate(newDate,DateUtil.DATE_TIME_FORMAT),
                            DateUtil.formatDate(newDate,DateUtil.DATE_TIME_FORMAT),null,0,DateUtil.formatDate(newDate,
                            DateUtil.DATE_TIME_FORMAT),0});
            }
        }
        if(!stockCardId.isClosed()){
            stockCardId.close();
        }
    }

    public void reSetLotsOnHand(List<String> productCodeList){
        Cursor lotsOnHandItemsResult=null;
        for (String productCode:productCodeList) {
            String getLotsOnHandItemsByStockCardId="SELECT * FROM lots_on_hand "
                    +"WHERE stockCard_id=(SELECT id FROM stock_cards WHERE product_id=(SELECT product_id FROM products WHERE code=?))";
            lotsOnHandItemsResult= LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(getLotsOnHandItemsByStockCardId,new String[]{productCode});
            while(lotsOnHandItemsResult.moveToNext()){
                if(lotsOnHandItemsResult.getInt(5)>0){
                    String reSetQuantityOnHandValue="UPDATE lots_on_hand SET quantityOnHand=0 WHERE id='"+lotsOnHandItemsResult.getInt(0)+"'";
                    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(reSetQuantityOnHandValue);
                }
            }
        }
        if(!lotsOnHandItemsResult.isClosed()){
            lotsOnHandItemsResult.close();
        }
    }
}