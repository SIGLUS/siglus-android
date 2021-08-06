package org.openlmis.core.model.repository;

import android.content.Context;
import android.database.Cursor;

import com.google.inject.Inject;
import com.j256.ormlite.dao.GenericRawResults;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;


public class StockMovementRepository {
    @Inject
    DbUtil dbUtil;
    @Inject
    Context context;

    @Inject
    private LotRepository lotRepository;

    GenericDao<StockMovementItem> genericDao;

    @Inject
    public StockMovementRepository(Context context) {
        genericDao = new GenericDao<>(StockMovementItem.class, context);
    }

    private void create(StockMovementItem stockMovementItem) throws LMISException {
        Date latestCreateTime = getLatestStockMovementCreatedTime();
        if (latestCreateTime != null && stockMovementItem.getCreatedTime().before(latestCreateTime)) {
            String productCode = stockMovementItem.getStockCard().getProduct().getCode();
            String facilityCode = UserInfoMgr.getInstance().getFacilityCode();
            LMISException e = new LMISException(facilityCode + ":" + productCode + ":" + (DateUtil.getCurrentDate()).toString());
            e.reportToFabric();
            throw e;
        }
        genericDao.create(stockMovementItem);
    }

    public List<StockMovementItem> listUnSynced() throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder().where().eq("synced", false)
                .and().isNotNull("stockCard_id").query());
    }

    protected void batchCreateOrUpdateStockMovementsAndLotInfo(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(StockMovementItem.class, (DbUtil.Operation<StockMovementItem, Void>) dao -> {
            for (StockMovementItem stockMovementItem : stockMovementItems) {
                updateDateTimeIfEmpty(stockMovementItem);
                dao.createOrUpdate(stockMovementItem);
                lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getLotMovementItemListWrapper());
            }
            return null;
        });
    }

    private void updateDateTimeIfEmpty(StockMovementItem stockMovementItem) {
        Date now = DateUtil.getCurrentDate();
        if (stockMovementItem.getCreatedTime() == null) {
            stockMovementItem.setCreatedTime(now);
        }
        stockMovementItem.setCreatedAt(DateUtil.getCurrentDate());
        stockMovementItem.setUpdatedAt(DateUtil.getCurrentDate());
    }

    public void batchCreateStockMovementItemAndLotItems(final StockMovementItem stockMovementItem) throws LMISException {
        stockMovementItem.setCreatedTime(new Date(LMISApp.getInstance().getCurrentTimeMillis()));
        // Create Stock Movement history list
        create(stockMovementItem);
        if (CollectionUtils.isNotEmpty(stockMovementItem.getLotMovementItemListWrapper())
                || CollectionUtils.isNotEmpty(stockMovementItem.getNewAddedLotMovementItemListWrapper())) {
            lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getLotMovementItemListWrapper());
            lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getNewAddedLotMovementItemListWrapper());
        }
    }

    public void batchCreateOrUpdateStockMovementsAndLotMovements(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(StockMovementItem.class, (DbUtil.Operation<StockMovementItem, Void>) dao -> {
            for (StockMovementItem stockMovementItem : stockMovementItems) {
                updateDateTimeIfEmpty(stockMovementItem);
                dao.createOrUpdate(stockMovementItem);
                for (LotMovementItem lotMovementItem : stockMovementItem.getLotMovementItemListWrapper()) {
                    Lot existingLot = lotRepository.getLotByLotNumberAndProductId(lotMovementItem.getLot().getLotNumber(), lotMovementItem.getLot().getProduct().getId());
                    lotMovementItem.setLot(existingLot);
                    lotRepository.createLotMovementItem(lotMovementItem);
                }
            }
            return null;
        });
    }

    public StockMovementItem getFirstStockMovement() throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
                .orderBy("movementDate", true)
                .orderBy("createdTime", true)
                .queryForFirst());
    }

    public Date getLatestStockMovementCreatedTime() throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao -> {
            try {
                final GenericRawResults<String[]> rawResults = dao.queryRaw("SELECT MAX(createdTime) FROM stock_items where movementDate=(SELECT MAX(movementDate) FROM stock_items)");
                final String[] firstResult = rawResults.getFirstResult();
                if (firstResult == null || firstResult.length <=0) {
                    return null;
                }
                return DateUtil.parseString(firstResult[0],DateUtil.DATE_TIME_FORMAT);
            }catch (Exception e){
                return null;
            }
        });
    }

    public List<String> queryStockMovementDatesByProgram(final String programCode) {
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

        return movementDates;
    }

    public Date queryEarliestStockMovementDateByProgram(final String programCode) {
        Date earliestDate = null;

        for (String movementDate : queryStockMovementDatesByProgram(programCode)) {
            Date date = DateUtil.parseString(movementDate, DB_DATE_FORMAT);
            if (earliestDate == null || (date != null && date.before(earliestDate))) {
                earliestDate = date;
            }
        }
        return earliestDate;
    }

    public StockMovementItem queryFirstStockMovementByStockCardId(final long stockCardId) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
                .orderBy("movementDate", true)
                .orderBy("createdTime", true)
                .where()
                .eq("stockCard_id", stockCardId)
                .queryForFirst());
    }

    public List<StockMovementItem> queryStockMovementHistory(final long stockCardId, final long startIndex, final long maxRows) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder().offset(startIndex).limit(maxRows)
                .orderBy("movementDate", true)
                .orderBy("createdTime", true)
                .orderBy("id", true)
                .where().eq("stockCard_id", stockCardId).query());
    }

    public List<StockMovementItem> queryStockItemsByCreatedDate(final long stockCardId, final Date periodBeginDate, final Date periodEndDate) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
                .orderBy("movementDate", true)
                .orderBy("createdTime", true)
                .where()
                .eq("stockCard_id", stockCardId)
                .and().gt("createdTime", periodBeginDate)//difference from the api above
                .and().le("createdTime", periodEndDate)
                .query());
    }

    //仅查找stockOnHand, movementQuantity, movementType用于填充RnrItem
    public List<StockMovementItem> queryNotFullFillStockItemsByCreatedData(final long stockCardId, final Date periodBeginDate, final Date periodEndDate){
        String rawSql = "SELECT stockOnHand, movementQuantity, movementType FROM stock_items WHERE stockCard_id='" + stockCardId + "'"
                + " AND createdTime > '" + DateUtil.formatDateTime(periodBeginDate) + "'"
                + " AND createdTime <= '" + DateUtil.formatDateTime(periodEndDate) + "'"
                + " ORDER BY movementDate, createdTime";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<StockMovementItem> stockItems = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                StockMovementItem stockMovementItem = new StockMovementItem();
                stockMovementItem.setStockOnHand(cursor.getLong(cursor.getColumnIndexOrThrow("stockOnHand")));
                stockMovementItem.setMovementQuantity(cursor.getLong(cursor.getColumnIndexOrThrow("movementQuantity")));
                stockMovementItem.setMovementType(MovementReasonManager.MovementType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("movementType"))));
                stockItems.add(stockMovementItem);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return stockItems;
    }

    public List<StockMovementItem> queryStockMovementsByMovementDate(final long stockCardId, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
                .orderBy("movementDate", true)
                .orderBy("createdTime", true)
                .where()
                .eq("stockCard_id", stockCardId)
                .and().ge("movementDate", startDate)
                .and().le("movementDate", endDate)
                .query());
    }

    public List<StockMovementItem> listLastFiveStockMovements(final long stockCardId) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao -> Lists.reverse(dao.queryBuilder().limit(5L)
                .orderBy("movementDate", false)
                .orderBy("createdTime", false)
                .orderBy("id", false)
                .where().eq("stockCard_id", stockCardId).query()));
    }

    public List<StockMovementItem> listLastTwoStockMovements() {
        String rawSql = "select * from stock_items as t1 where t1.id in "
                + "(select t2.id from stock_items as t2 where t1.stockCard_id = t2.stockCard_id "
                + "order by t2.movementDate desc,  t2.createdTime desc, id desc limit 2)";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<StockMovementItem> items = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                StockMovementItem item = new StockMovementItem();
                Date createTime = DateUtil.parseString(cursor.getString(cursor.getColumnIndexOrThrow("createdTime")), DateUtil.DATE_TIME_FORMAT);
                item.setCreatedTime(createTime);
                Date movementDate = DateUtil.parseString(cursor.getString(cursor.getColumnIndexOrThrow("movementDate")), DB_DATE_FORMAT);
                item.setMovementDate(movementDate);
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                item.setStockOnHand(cursor.getInt(cursor.getColumnIndexOrThrow("stockOnHand")));
                item.setMovementQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("movementQuantity")));
                item.setMovementType(MovementReasonManager.MovementType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("movementType"))));
                StockCard stockCard = new StockCard();
                stockCard.setId(cursor.getInt(cursor.getColumnIndexOrThrow("stockCard_id")));
                item.setStockCard(stockCard);
                items.add(item);

            } while (cursor.moveToNext());
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        return items;
    }

    public List<StockMovementItem> queryMovementByStockCardId(final long stockCardId) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, dao ->
                dao.queryBuilder().orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .orderBy("id", true)
                        .where().eq("stockCard_id", stockCardId).query()
        );
    }

    public Map<String, List<StockMovementItem>> queryStockMovement(Set<String> stockCardIds) {
        String ids = StringUtils.join(stockCardIds != null ? stockCardIds : new HashSet<>(), ',');
        String rawSql = "select stockCard_id, GROUP_CONCAT(id || ',' || movementType || ',' "
                + "|| movementQuantity || ',' || stockOnHand || ',' || movementDate || ',' "
                + "|| createdTime || ',' || reason ,  ';') as movementItems "
                + "from stock_items where stockCard_id in ( "
                + ids
                + ")  GROUP BY stockCard_id";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        Map<String, List<StockMovementItem>> stockCardsMovements = new HashMap<>();
        if (cursor.moveToFirst()) {
            do {
                getStockMovementItems(stockCardsMovements, cursor);
            } while (cursor.moveToNext());
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        Map<String, List<StockMovementItem>> fullyStockCardsMovements = new HashMap<>();
        for (String stockCardId : stockCardIds) {
            List<StockMovementItem> movementItems = new ArrayList<>();
            if (stockCardsMovements.containsKey(stockCardId)) {
                movementItems = stockCardsMovements.get(stockCardId);
            }
            fullyStockCardsMovements.put(stockCardId, movementItems);
        }
        return fullyStockCardsMovements;

    }

    public List<StockMovementItem> queryEachStockCardNewestMovement() {
        String rawSql = "SELECT *,MAX(createdTime) FROM stock_items GROUP BY stockCard_id";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<StockMovementItem> items = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                StockMovementItem item = new StockMovementItem();
                Date createTime = DateUtil.parseString(cursor.getString(cursor.getColumnIndexOrThrow("createdTime")), DateUtil.DATE_TIME_FORMAT);
                item.setCreatedTime(createTime);
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                item.setStockOnHand(cursor.getInt(cursor.getColumnIndexOrThrow("stockOnHand")));
                items.add(item);
            } while (cursor.moveToNext());
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        return items;
    }


    public void deleteStockMovementItems(final StockCard stockCard) throws LMISException {
        List<StockMovementItem> items = dbUtil.withDao(StockMovementItem.class,
                dao -> dao.queryBuilder()
                        .where().eq("stockCard_id", stockCard.getId())
                        .query());
        for (StockMovementItem item : items) {
            genericDao.delete(item);
        }
    }

    public List<String> signatureIsNull() throws LMISException {
        List<String> stockCardIds = new ArrayList<>();
        String querySql = "select stockCard_id,count(stockCard_id) as res from stock_items where signature IS NULL group by stockCard_id having res > 1";
        GenericRawResults<String[]> rawResults = dbUtil.withDao(StockMovementItem.class, dao -> dao.queryRaw(querySql));
        try {
            for (String[] resultArray : rawResults) {
                stockCardIds.add(resultArray[0]);
            }
            rawResults.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockCardIds;
    }

    public void deleteStockMovementItems(List<StockMovementItem> deletedStockMovementItems) {
        if (deletedStockMovementItems == null) return;
        Set<String> stockItemIds = new HashSet<>();
        for (StockMovementItem item : deletedStockMovementItems) {
            stockItemIds.add(String.valueOf(item.getId()));
        }
        String deleteRowSql = "delete from stock_items where id in ("
                + StringUtils.join(stockItemIds, ",")
                + ")";
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRowSql);
    }

    public Map<String, List<StockMovementItem>> queryNoSignatureStockCardsMovements() {
        String selectResult = "select stockCard_id, GROUP_CONCAT(id || ',' || movementType || ',' "
                + "|| movementQuantity || ',' || stockOnHand || ',' || movementDate || ',' "
                + "|| createdTime || ',' || reason ,  ';') as movementItems ,count(*) as count ";
        String stockCardHavingSignatureNotNull = "( select stockCard_id from stock_items where signature not null group by stockCard_id ) ";
        String querySql = selectResult
                + "from stock_items "
                + "where stockCard_id not in "
                + stockCardHavingSignatureNotNull
                + "group by stockCard_id having count >1;";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(querySql, null);
        Map<String, List<StockMovementItem>> stockCardsMovements = new HashMap<>();
        if (cursor.moveToFirst()) {
            do {
                getStockMovementItems(stockCardsMovements, cursor);
            } while (cursor.moveToNext());
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        return stockCardsMovements;
    }

    public Map<String, String> queryStockCardIdAndProductCode(Set<String> stockCardsIds) {
        String stockCardIds = StringUtils.join(stockCardsIds != null ? stockCardsIds : new HashSet<>(), ',');
        String querySql = "select stock_cards.id, products.code from stock_cards "
                + "join products on stock_cards.product_id = products.id where stock_cards.id in  ( "
                + stockCardIds
                + ");";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(querySql, null);
        Map<String, String> cardMapProductCode = new HashMap<>();
        if (cursor.moveToFirst()) {
            do {
                String stockCardId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
                cardMapProductCode.put(stockCardId, code);
            } while (cursor.moveToNext());
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        return cardMapProductCode;
    }

    public Map<String, List<StockMovementItem>> queryHavingSignatureAndDuplicatedDirtyDataNoAffectCalculatedStockCardsMovements(Set<String> filterStockCards) {
        String filterIds = StringUtils.join(filterStockCards != null ? filterStockCards : new HashSet<>(), ',');
        String selectResult = "select stockCard_id, GROUP_CONCAT(id || ',' || movementType || ',' "
                + "|| movementQuantity || ',' || stockOnHand || ',' || movementDate || ',' "
                + "|| createdTime || ',' || reason ,  ';') as movementItems ";
        String havingDuplicatedNoSignature = "( select stockCard_id from stock_items where signature IS NULL and stockCard_id not in ( "
                + filterIds
                + ") group by stockCard_id having count(stockCard_id) > 1) ";
        String minSignatureTimeForHavingDirtyData = "( select stockCard_id, min(movementDate) as minMovementDate, "
                + "min(createdTime) as minCreatedTime from stock_items where stockCard_id in "
                + havingDuplicatedNoSignature
                + "and signature not null group by stockCard_id) as minSignatureTimeTable ";
        String joinStockItemAndMinSignatureTime = "(select stock_items.*, minSignatureTimeTable.* "
                + "from stock_items left join "
                + minSignatureTimeForHavingDirtyData
                + "on minSignatureTimeTable.stockCard_id = stock_items.stockCard_id ) as result ";
        String querySql = selectResult
                + "from "
                + joinStockItemAndMinSignatureTime
                + "where result.minMovementDate not null "
                + "and result.MovementDate <= result.minMovementDate "
                + "and result.createdTime <= minCreatedTime "
                + "group by stockCard_id having count(stockCard_id) >2 ";

        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(querySql, null);
        Map<String, List<StockMovementItem>> stockCardsMovements = new HashMap<>();
        if (cursor.moveToFirst()) {
            do {
                getStockMovementItems(stockCardsMovements, cursor);
            } while (cursor.moveToNext());
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        return stockCardsMovements;
    }

    private void getStockMovementItems(Map<String, List<StockMovementItem>> stockCardsMovements, Cursor cursor) {
        String stockCardId = cursor.getString(cursor.getColumnIndexOrThrow("stockCard_id"));
        List<StockMovementItem> stockMovementItems = new ArrayList<>();
        String strMovementItems = cursor.getString(cursor.getColumnIndexOrThrow("movementItems"));
        String[] listMovementItems = strMovementItems.split(";");
        for (String strMovementItem : listMovementItems) {
            String[] listMovementItem = strMovementItem.split(",");
            StockMovementItem movementItem = new StockMovementItem();
            StockCard stockCard = new StockCard();
            stockCard.setId(Integer.parseInt(stockCardId));
            movementItem.setStockCard(stockCard);
            movementItem.setId(Long.parseLong(listMovementItem[0]));
            movementItem.setMovementType(MovementReasonManager.MovementType.valueOf(listMovementItem[1]));
            movementItem.setMovementQuantity(Long.parseLong(listMovementItem[2]));
            movementItem.setStockOnHand(Long.parseLong(listMovementItem[3]));
            Date movementDate = DateUtil.parseString(listMovementItem[4], DateUtil.DB_DATE_FORMAT);
            movementItem.setMovementDate(movementDate);
            Date createTime = DateUtil.parseString(listMovementItem[5], DateUtil.DATE_TIME_FORMAT);
            movementItem.setCreatedTime(createTime);
            movementItem.setReason(listMovementItem[6]);
            stockMovementItems.add(movementItem);
        }
        SortClass sort = new SortClass();
        Collections.sort(stockMovementItems,sort);
        stockCardsMovements.put(stockCardId, stockMovementItems);
    }


    public static class SortClass implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            StockMovementItem item1 = (StockMovementItem) o1;
            StockMovementItem item2 = (StockMovementItem) o2;
            int compareMovementDate = item1.getMovementDate().compareTo(item2.getMovementDate());
            if (compareMovementDate == 0) {
                int compareCreatedTime = item1.getCreatedTime().compareTo(item2.getCreatedTime());
                if (compareCreatedTime == 0) {
                    int comparedId = item1.getId() > item2.getId() ? 1 : -1;
                    return comparedId;
                }
                return compareCreatedTime;
            } else {
                return compareMovementDate;
            }
        }
    }

    public void resetKeepItemToNotSynced(Map<String,List<StockMovementItem>> stockMovementItemsMap) {
        List<String> keepMovements = new ArrayList<>();
        for (Map.Entry map : stockMovementItemsMap.entrySet()) {
            keepMovements.add(String.valueOf(stockMovementItemsMap.get(map.getKey()).get(0).getId()));
        }
        String updateSql = "update stock_items set synced = 0 where id in ("
                + StringUtils.join(keepMovements,",")+ ")";
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(updateSql);
    }

}