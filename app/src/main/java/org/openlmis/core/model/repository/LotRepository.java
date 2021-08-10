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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseModel;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class LotRepository {

  @Inject
  DbUtil dbUtil;

  GenericDao<Lot> lotGenericDao;
  GenericDao<LotOnHand> lotOnHandGenericDao;
  GenericDao<LotMovementItem> lotMovementItemGenericDao;

  @Inject
  public LotRepository(Context context) {
    lotGenericDao = new GenericDao<>(Lot.class, context);
    lotOnHandGenericDao = new GenericDao<>(LotOnHand.class, context);
    lotMovementItemGenericDao = new GenericDao<>(LotMovementItem.class, context);
  }

  public void batchCreateLotsAndLotMovements(final List<LotMovementItem> lotMovementItemListWrapper)
      throws LMISException {
    dbUtil.withDaoAsBatch(LotMovementItem.class, dao -> {
      for (final LotMovementItem lotMovementItem : lotMovementItemListWrapper) {
        createOrUpdateLotAndLotOnHand(lotMovementItem);
        if (null == lotMovementItem.getMovementQuantity()) {
          lotMovementItem.setMovementQuantity(0L);
        }
        createLotMovementItem(lotMovementItem);
      }
      return null;
    });
  }

  private void createOrUpdateLotAndLotOnHand(LotMovementItem lotMovementItem) throws LMISException {
    final Lot lot = lotMovementItem.getLot();
    Lot existingLot = getLotByLotNumberAndProductId(lot.getLotNumber(), lot.getProduct().getId());
    LotOnHand lotOnHand;

    if (null != lotMovementItem.getMovementQuantity()) {
      if (existingLot == null) {
        lot.setCreatedAt(DateUtil.getCurrentDate());
        lot.setUpdatedAt(DateUtil.getCurrentDate());
        createOrUpdateLot(lot);

        lotOnHand = new LotOnHand(lot, lotMovementItem.getStockMovementItem().getStockCard(),
            lotMovementItem.getMovementQuantity());
        createOrUpdateLotOnHand(lotOnHand);

        lotMovementItem.setStockOnHand(lotMovementItem.getMovementQuantity());
      } else {
        lotOnHand = getLotOnHandByLot(existingLot);
        if (lotOnHand.getQuantityOnHand() == 0) {
          existingLot.setExpirationDate(lot.getExpirationDate());
          createOrUpdateLot(existingLot);
        }
        if (lotMovementItem.isStockOnHandReset()) {
          lotOnHand.setQuantityOnHand(lotMovementItem.getStockOnHand());
        } else {
          lotOnHand.setQuantityOnHand(
              lotOnHand.getQuantityOnHand() + lotMovementItem.getMovementQuantity());
        }
        createOrUpdateLotOnHand(lotOnHand);
        lotMovementItem.setLot(existingLot);
        lotMovementItem.setStockOnHand(lotOnHand.getQuantityOnHand());
      }
    } else {
      if (existingLot == null) {
        lotMovementItem.setStockOnHand(0L);
      } else {
        lotMovementItem.setLot(existingLot);
        lotOnHand = getLotOnHandByLot(existingLot);
        lotMovementItem.setStockOnHand(lotOnHand.getQuantityOnHand());
      }
    }
  }

  public void createOrUpdateLot(final Lot lot) throws LMISException {
    dbUtil.withDao(Lot.class, dao -> {
      lot.setLotNumber(lot.getLotNumber().toUpperCase());
      dao.createOrUpdate(lot);
      return null;
    });
  }

  public Lot createOrUpdate(final Lot lot) throws LMISException {
    Lot existingLot = getLotByLotNumberAndProductId(lot.getLotNumber(), lot.getProduct().getId());
    if (existingLot != null) {
      lot.setId(existingLot.getId());
    }
    return lotGenericDao.createOrUpdate(lot);
  }

  private void createOrUpdateLotOnHand(final LotOnHand finalLotOnHand) throws LMISException {
    dbUtil.withDao(LotOnHand.class, dao -> {
      dao.createOrUpdate(finalLotOnHand);
      return null;
    });
  }

  public void createLotMovementItem(final LotMovementItem lotMovementItem) throws LMISException {
    lotMovementItem.setCreatedAt(DateUtil.getCurrentDate());
    lotMovementItem.setUpdatedAt(DateUtil.getCurrentDate());

    dbUtil.withDao(LotMovementItem.class, dao -> {
      dao.createOrUpdate(lotMovementItem);
      return null;
    });
  }

  public LotOnHand getLotOnHandByLot(final Lot lot) throws LMISException {
    return dbUtil.withDao(LotOnHand.class, dao -> dao.queryBuilder()
        .where()
        .eq(FieldConstants.LOT_ID, lot.getId())
        .queryForFirst());
  }

  public Lot getLotByLotNumberAndProductId(final String lotNumber, final long productId)
      throws LMISException {
    return dbUtil.withDao(Lot.class, dao -> dao.queryBuilder()
        .where()
        .eq(FieldConstants.LOT_NUMBER, lotNumber.toUpperCase())
        .and()
        .eq(FieldConstants.PRODUCT_ID, productId)
        .queryForFirst());
  }

  public void createOrUpdateLotsInformation(final List<LotOnHand> lotOnHandListWrapper)
      throws LMISException {
    dbUtil.withDaoAsBatch(LotOnHand.class, dao -> {
      for (final LotOnHand lotOnHand : lotOnHandListWrapper) {
        createOrUpdateLot(lotOnHand.getLot());
        createOrUpdateLotOnHand(lotOnHand);
      }
      return null;
    });
  }

  public void deleteLotInfo(final StockCard stockCard) throws LMISException {
    List<Lot> lots = dbUtil.withDao(Lot.class, dao -> dao.queryBuilder()
        .where().eq(FieldConstants.PRODUCT_ID, stockCard.getProduct() != null ? stockCard.getProduct().getId() : "null")
        .query());
    List<LotOnHand> lotOnHands = dbUtil.withDao(LotOnHand.class, dao -> dao.queryBuilder()
        .where().eq(FieldConstants.STOCK_CARD_ID, stockCard.getId())
        .query());
    List<Long> lotIds = FluentIterable.from(lots).transform(BaseModel::getId).toList();
    List<LotMovementItem> lotMovementItems = dbUtil.withDao(LotMovementItem.class, dao -> dao.queryBuilder()
        .where().in(FieldConstants.LOT_ID, lotIds)
        .query());

    for (LotMovementItem item : lotMovementItems) {
      lotMovementItemGenericDao.delete(item);
    }
    for (LotOnHand lotOnHand : lotOnHands) {
      lotOnHandGenericDao.delete(lotOnHand);
    }
    for (Lot lot : lots) {
      lotGenericDao.delete(lot);
    }
  }

  public void deleteLotMovementItems(List<StockMovementItem> stockMovementItems) {
    List<String> stockMovementItemIds = new ArrayList<>();
    for (StockMovementItem item : stockMovementItems) {
      stockMovementItemIds.add(String.valueOf(item.getId()));
    }
    String deleteRowSql = "delete from lot_movement_items where stockMovementItem_id in ("
        + StringUtils.join(stockMovementItemIds, ",")
        + ")";
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(deleteRowSql);
  }

  public List<LotMovementItem> resetKeepLotMovementItems(
      Map<String, List<StockMovementItem>> stockMovementItemsMap) {
    Set<String> keepMovementIds = new HashSet<>();
    for (Map.Entry<String, List<StockMovementItem>> map : stockMovementItemsMap.entrySet()) {
      List<StockMovementItem> items = map.getValue();
      for (StockMovementItem item : items) {
        keepMovementIds.add(String.valueOf(item.getId()));
      }
    }
    try {
      List<LotMovementItem> lotMovementItems = getLotMovementByStockMovement(keepMovementIds);
      for (LotMovementItem lotMovementItem : lotMovementItems) {
        lotMovementItem.setStockOnHand(lotMovementItem.getMovementQuantity());
      }
      dbUtil
          .withDaoAsBatch(LotMovementItem.class, dao -> {
            for (LotMovementItem lotMovementItem : lotMovementItems) {
              dao.createOrUpdate(lotMovementItem);
            }
            return null;
          });
      return lotMovementItems;
    } catch (LMISException e) {
      new LMISException(e, "movementItems.get").reportToFabric();
    }
    return new ArrayList<>();
  }

  private List<LotMovementItem> getLotMovementByStockMovement(final Set<String> stockMovementIds)
      throws LMISException {
    return dbUtil.withDao(LotMovementItem.class, dao -> dao.queryBuilder()
        .where()
        .in("stockMovementItem_id", stockMovementIds)
        .query());
  }
}
