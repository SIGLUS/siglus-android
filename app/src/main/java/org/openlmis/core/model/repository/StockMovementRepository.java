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

import static org.openlmis.core.constant.FieldConstants.CODE;
import static org.openlmis.core.constant.FieldConstants.CREATED_TIME;
import static org.openlmis.core.constant.FieldConstants.ID;
import static org.openlmis.core.constant.FieldConstants.MOVEMENT_DATE;
import static org.openlmis.core.constant.FieldConstants.MOVEMENT_QUANTITY;
import static org.openlmis.core.constant.FieldConstants.MOVEMENT_TYPE;
import static org.openlmis.core.constant.FieldConstants.STOCK_CARD_ID;
import static org.openlmis.core.constant.FieldConstants.STOCK_ON_HAND;
import static org.openlmis.core.constant.FieldConstants.SYNCED;
import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;

import android.content.Context;
import android.database.Cursor;
import com.google.inject.Inject;
import com.j256.ormlite.dao.GenericRawResults;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.Lists;

public class StockMovementRepository {

  private static final String SELECT_RESULT =
      "select stockCard_id, GROUP_CONCAT(id || ',' || movementType || ',' || movementQuantity || ',' || stockOnHand || "
          + "',' || movementDate || ',' || createdTime || ',' || reason ,  ';') as movementItems ";
  @Inject
  DbUtil dbUtil;
  @Inject
  Context context;

  GenericDao<StockMovementItem> genericDao;

  @Inject
  private LotRepository lotRepository;

  @Inject
  public StockMovementRepository(Context context) {
    genericDao = new GenericDao<>(StockMovementItem.class, context);
  }

  private void create(StockMovementItem stockMovementItem) throws LMISException {
    Date latestMovementDate = getLatestStockMovementCreatedTime();
    if (latestMovementDate != null && stockMovementItem.getCreatedTime().before(latestMovementDate)) {
      String productCode = stockMovementItem.getStockCard().getProduct().getCode();
      String facilityCode = UserInfoMgr.getInstance().getFacilityCode();
      LMISException e = new LMISException(
          facilityCode + ":" + productCode + ":" + (DateUtil.getCurrentDate()).toString());
      e.reportToFabric();
      throw e;
    }
    genericDao.create(stockMovementItem);
  }

  public List<StockMovementItem> listUnSynced() throws LMISException {
    return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder().where().eq(SYNCED, false)
        .and().isNotNull(STOCK_CARD_ID).query());
  }

  protected void batchCreateOrUpdateStockMovementsAndLotInfo(
      final List<StockMovementItem> stockMovementItems) throws LMISException {
    dbUtil.withDaoAsBatch(StockMovementItem.class,
        dao -> {
          for (StockMovementItem stockMovementItem : stockMovementItems) {
            updateDateTimeIfEmpty(stockMovementItem);
            dao.createOrUpdate(stockMovementItem);
            List<LotMovementItem> lotMovementItems = FluentIterable.from(
                stockMovementItem.getLotMovementItemListWrapper())
                .transform(lotMovementItem -> {
                  lotMovementItem.setReason(stockMovementItem.getReason());
                  lotMovementItem.setDocumentNumber(stockMovementItem.getDocumentNumber());
                  return lotMovementItem;
                }).toList();
            lotRepository
                .batchCreateLotsAndLotMovements(lotMovementItems);
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

  public void batchCreateStockMovementItemAndLotItemsForProductOperation(final StockMovementItem stockMovementItem)
      throws LMISException {
    stockMovementItem.setCreatedTime(new Date(LMISApp.getInstance().getCurrentTimeMillis()));
    // Create Stock Movement history list
    create(stockMovementItem);
    if (CollectionUtils.isNotEmpty(stockMovementItem.getLotMovementItemListWrapper())
        || CollectionUtils.isNotEmpty(stockMovementItem.getNewAddedLotMovementItemListWrapper())) {
      lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getLotMovementItemListWrapper());
      lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getNewAddedLotMovementItemListWrapper());
    }
  }

  public void batchCreateOrUpdateStockMovementsAndLotMovements(
      final List<StockMovementItem> stockMovementItems) throws LMISException {
    dbUtil.withDaoAsBatch(StockMovementItem.class,
        dao -> {
          for (StockMovementItem stockMovementItem : stockMovementItems) {
            updateDateTimeIfEmpty(stockMovementItem);
            dao.createOrUpdate(stockMovementItem);
            for (LotMovementItem lotMovementItem : stockMovementItem
                .getLotMovementItemListWrapper()) {
              Lot existingLot = lotRepository
                  .getLotByLotNumberAndProductId(lotMovementItem.getLot().getLotNumber(),
                      lotMovementItem.getLot().getProduct().getId());
              lotMovementItem.setLot(existingLot);
              lotRepository.createLotMovementItem(lotMovementItem);
            }
          }
          return null;
        });
  }

  public StockMovementItem getFirstStockMovement() throws LMISException {
    return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
        .orderBy(MOVEMENT_DATE, true)
        .orderBy(CREATED_TIME, true)
        .queryForFirst());
  }

  public Date getLatestStockMovementCreatedTime() throws LMISException {
    return dbUtil.withDao(StockMovementItem.class, dao -> {
      try {
        final GenericRawResults<String[]> rawResults = dao.queryRaw(
            "SELECT MAX(createdTime) FROM stock_items where movementDate="
                + "(SELECT MAX(movementDate) FROM stock_items)");
        final String[] firstResult = rawResults.getFirstResult();
        if (firstResult == null || firstResult.length <= 0) {
          return null;
        }
        return DateUtil.parseString(firstResult[0], DateUtil.DATE_TIME_FORMAT);
      } catch (Exception e) {
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
        + "WHERE p1.isActive = 1 AND p1.isArchived = 0 AND p2.isActive = 1 AND p3.programCode = '"
        + programCode + "' "
        + "OR p3.parentCode = '" + programCode + "'";
    final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase().rawQuery(rawSql, null);
    List<String> movementDates = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        movementDates.add(cursor.getString(cursor.getColumnIndexOrThrow(MOVEMENT_DATE)));
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

  public StockMovementItem queryFirstStockMovementByStockCardId(final long stockCardId)
      throws LMISException {
    return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
        .orderBy(MOVEMENT_DATE, true)
        .orderBy(CREATED_TIME, true)
        .where().eq(STOCK_CARD_ID, stockCardId)
        .queryForFirst());
  }

  public List<StockMovementItem> queryStockMovementHistory(final long stockCardId,
      final long startIndex, final long maxRows) throws LMISException {
    return dbUtil.withDao(StockMovementItem.class,
        dao -> dao.queryBuilder().offset(startIndex).limit(maxRows)
            .orderBy(MOVEMENT_DATE, true)
            .orderBy(CREATED_TIME, true)
            .orderBy(ID, true)
            .where().eq(STOCK_CARD_ID, stockCardId).query());
  }

  public List<StockMovementItem> queryStockItemsByCreatedDate(final long stockCardId,
      final Date periodBeginDate, final Date periodEndDate) throws LMISException {
    return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
        .orderBy(MOVEMENT_DATE, true)
        .orderBy(CREATED_TIME, true)
        .where().eq(STOCK_CARD_ID, stockCardId)
        .and().gt(CREATED_TIME, periodBeginDate)//difference from the api above
        .and().le(CREATED_TIME, periodEndDate)
        .query());
  }

  //仅查找stockOnHand, movementQuantity, movementType用于填充RnrItem
  public List<StockMovementItem> queryNotFullFillStockItemsByCreatedData(final long stockCardId,
      final Date periodBeginDate, final Date periodEndDate) {
    String rawSql =
        "SELECT stockOnHand, movementQuantity, movementType FROM stock_items WHERE stockCard_id='"
            + stockCardId + "'"
            + " AND movementDate >= '" + DateUtil.formatDateTimeToDay(periodBeginDate) + "'"
            + " AND movementDate <= '" + DateUtil.formatDateTimeToDay(periodEndDate) + "'"
            + " ORDER BY movementDate, createdTime";
    final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .rawQuery(rawSql, null);
    List<StockMovementItem> stockItems = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setStockOnHand(cursor.getLong(cursor.getColumnIndexOrThrow(STOCK_ON_HAND)));
        stockMovementItem.setMovementQuantity(cursor.getLong(cursor.getColumnIndexOrThrow(MOVEMENT_QUANTITY)));
        stockMovementItem.setMovementType(
            MovementReasonManager.MovementType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(MOVEMENT_TYPE))));
        stockItems.add(stockMovementItem);
      } while (cursor.moveToNext());
    }
    if (!cursor.isClosed()) {
      cursor.close();
    }
    return stockItems;
  }

  public List<StockMovementItem> queryStockMovementsByMovementDate(final long stockCardId,
      final Date startDate, final Date endDate) throws LMISException {
    return dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
        .orderBy(MOVEMENT_DATE, true)
        .orderBy(CREATED_TIME, true)
        .where().eq(STOCK_CARD_ID, stockCardId)
        .and().ge(MOVEMENT_DATE, startDate)
        .and().le(MOVEMENT_DATE, endDate)
        .query());
  }

  public List<StockMovementItem> listLastFiveStockMovements(final long stockCardId) throws LMISException {
    return dbUtil.withDao(StockMovementItem.class, dao -> Lists.reverse(dao.queryBuilder().limit(5L)
        .orderBy(MOVEMENT_DATE, false)
        .orderBy(CREATED_TIME, false)
        .orderBy(ID, false)
        .where().eq(STOCK_CARD_ID, stockCardId)
        .query()));
  }

  public List<StockMovementItem> listLastTwoStockMovements() {
    String rawSql = "select * from stock_items as t1 where t1.id in "
        + "(select t2.id from stock_items as t2 where t1.stockCard_id = t2.stockCard_id "
        + "order by t2.movementDate desc,  t2.createdTime desc, id desc limit 2)";
    final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase().rawQuery(rawSql, null);
    List<StockMovementItem> items = new ArrayList<>();

    if (cursor.moveToFirst()) {
      do {
        StockMovementItem item = new StockMovementItem();
        Date createTime = DateUtil
            .parseString(cursor.getString(cursor.getColumnIndexOrThrow(CREATED_TIME)),
                DateUtil.DATE_TIME_FORMAT_WITH_MS);
        item.setCreatedTime(createTime);
        Date movementDate = DateUtil
            .parseString(cursor.getString(cursor.getColumnIndexOrThrow(MOVEMENT_DATE)), DB_DATE_FORMAT);
        item.setMovementDate(movementDate);
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ID)));
        item.setStockOnHand(cursor.getInt(cursor.getColumnIndexOrThrow(STOCK_ON_HAND)));
        item.setMovementQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(MOVEMENT_QUANTITY)));
        item.setMovementType(
            MovementReasonManager.MovementType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(MOVEMENT_TYPE))));
        StockCard stockCard = new StockCard();
        stockCard.setId(cursor.getInt(cursor.getColumnIndexOrThrow(STOCK_CARD_ID)));
        item.setStockCard(stockCard);
        items.add(item);

      } while (cursor.moveToNext());
    }

    if (!cursor.isClosed()) {
      cursor.close();
    }
    return items;
  }

  public List<StockMovementItem> queryMovementByStockCardId(final long stockCardId)
      throws LMISException {
    return dbUtil.withDao(StockMovementItem.class, dao ->
        dao.queryBuilder()
            .orderBy(MOVEMENT_DATE, true)
            .orderBy(CREATED_TIME, true)
            .orderBy(ID, true)
            .where().eq(STOCK_CARD_ID, stockCardId)
            .query()
    );
  }

  public Map<String, List<StockMovementItem>> queryStockMovement(Set<String> stockCardIds) {
    String ids = StringUtils.join(stockCardIds != null ? stockCardIds : new HashSet<>(), ',');
    String rawSql = SELECT_RESULT
        + "from stock_items where stockCard_id in ( "
        + ids
        + ")  GROUP BY stockCard_id";
    final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase().rawQuery(rawSql, null);
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

  public void deleteStockMovementItems(final StockCard stockCard) throws LMISException {
    List<StockMovementItem> items = dbUtil.withDao(StockMovementItem.class, dao -> dao.queryBuilder()
        .where().eq(STOCK_CARD_ID, stockCard.getId())
        .query());
    for (StockMovementItem item : items) {
      genericDao.delete(item);
    }
  }

  public void deleteStockMovementItems(List<StockMovementItem> deletedStockMovementItems) {
    if (deletedStockMovementItems == null) {
      return;
    }
    Set<String> stockItemIds = new HashSet<>();
    for (StockMovementItem item : deletedStockMovementItems) {
      stockItemIds.add(String.valueOf(item.getId()));
    }
    String deleteRowSql = "delete from stock_items where id in ("
        + StringUtils.join(stockItemIds, ",")
        + ")";
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(deleteRowSql);
  }

  public Map<String, List<StockMovementItem>> queryNoSignatureStockCardsMovements() {
    String selectResult = SELECT_RESULT + ",count(*) as count ";
    String stockCardHavingSignatureNotNull = "( select stockCard_id from stock_items "
        + "where signature not null group by stockCard_id ) ";
    String querySql = selectResult
        + "from stock_items "
        + "where stockCard_id not in "
        + stockCardHavingSignatureNotNull
        + "group by stockCard_id having count >1;";
    final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase().rawQuery(querySql, null);
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
    String stockCardIds = StringUtils
        .join(stockCardsIds != null ? stockCardsIds : new HashSet<>(), ',');
    String querySql = "select stock_cards.id, products.code from stock_cards "
        + "join products on stock_cards.product_id = products.id where stock_cards.id in  ( "
        + stockCardIds
        + ");";
    final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase().rawQuery(querySql, null);
    Map<String, String> cardMapProductCode = new HashMap<>();
    if (cursor.moveToFirst()) {
      do {
        String stockCardId = cursor.getString(cursor.getColumnIndexOrThrow(ID));
        String code = cursor.getString(cursor.getColumnIndexOrThrow(CODE));
        cardMapProductCode.put(stockCardId, code);
      } while (cursor.moveToNext());
    }

    if (!cursor.isClosed()) {
      cursor.close();
    }
    return cardMapProductCode;
  }

  public Map<String, List<StockMovementItem>> queryDirtyDataNoAffectCalculatedStockCardsMovements(
      Set<String> filterStockCards) {
    String filterIds = StringUtils.join(filterStockCards != null ? filterStockCards : new HashSet<>(), ',');
    String selectResult = SELECT_RESULT;
    String havingDuplicatedNoSignature =
        "( select stockCard_id from stock_items where signature IS NULL and stockCard_id not in ( "
            + filterIds
            + ") group by stockCard_id having count(stockCard_id) > 1) ";
    String minSignatureTimeForHavingDirtyData =
        "( select stockCard_id, min(movementDate) as minMovementDate, "
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

    final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase().rawQuery(querySql, null);
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

  private void getStockMovementItems(Map<String, List<StockMovementItem>> stockCardsMovements,
      Cursor cursor) {
    String stockCardId = cursor.getString(cursor.getColumnIndexOrThrow(STOCK_CARD_ID));
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
      Date createTime = DateUtil.parseString(listMovementItem[5], DateUtil.DATE_TIME_FORMAT_WITH_MS);
      movementItem.setCreatedTime(createTime);
      movementItem.setReason(listMovementItem[6]);
      stockMovementItems.add(movementItem);
    }
    SortClass sort = new SortClass();
    Collections.sort(stockMovementItems, sort);
    stockCardsMovements.put(stockCardId, stockMovementItems);
  }

  public void resetKeepItemToNotSynced(Map<String, List<StockMovementItem>> stockMovementItemsMap) {
    List<String> keepMovements = new ArrayList<>();
    for (Map.Entry<String, List<StockMovementItem>> map : stockMovementItemsMap.entrySet()) {
      keepMovements.add(String.valueOf(stockMovementItemsMap.get(map.getKey()).get(0).getId()));
    }
    String updateSql = "update stock_items set synced = 0 where id in ("
        + StringUtils.join(keepMovements, ",") + ")";
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(updateSql);
  }

  public static class SortClass implements Comparator<StockMovementItem> {

    @Override
    public int compare(StockMovementItem o1, StockMovementItem o2) {
      int compareMovementDate = o1.getMovementDate().compareTo(o2.getMovementDate());
      if (compareMovementDate == 0) {
        int compareCreatedTime = o1.getCreatedTime().compareTo(o2.getCreatedTime());
        if (compareCreatedTime == 0) {
          return o1.getId() > o2.getId() ? 1 : -1;
        }
        return compareCreatedTime;
      } else {
        return compareMovementDate;
      }
    }
  }

}