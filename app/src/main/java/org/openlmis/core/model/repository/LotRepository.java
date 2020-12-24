package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LotRepository {

    @Inject
    DbUtil dbUtil;
    @Inject
    Context context;

    GenericDao<Lot> lotGenericDao;
    GenericDao<LotOnHand> lotOnHandGenericDao;
    GenericDao<LotMovementItem> lotMovementItemGenericDao;

    @Inject
    public LotRepository(Context context) {
        lotGenericDao = new GenericDao<>(Lot.class, context);
        lotOnHandGenericDao = new GenericDao<>(LotOnHand.class, context);
        lotMovementItemGenericDao = new GenericDao<>(LotMovementItem.class, context);
    }

    public void batchCreateLotsAndLotMovements(final List<LotMovementItem> lotMovementItemListWrapper) throws LMISException {
        dbUtil.withDaoAsBatch(LotMovementItem.class, dao -> {
            for (final LotMovementItem lotMovementItem : lotMovementItemListWrapper) {
                createOrUpdateLotAndLotOnHand(lotMovementItem);
                if (null != lotMovementItem.getMovementQuantity()) {
                    createLotMovementItem(lotMovementItem);
                }
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
                lot.setCreatedAt(new Date());
                lot.setUpdatedAt(new Date());
                createOrUpdateLot(lot);

                lotOnHand = new LotOnHand(lot, lotMovementItem.getStockMovementItem().getStockCard(), lotMovementItem.getMovementQuantity());
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
                    lotOnHand.setQuantityOnHand(lotOnHand.getQuantityOnHand() + lotMovementItem.getMovementQuantity());
                }
                createOrUpdateLotOnHand(lotOnHand);
                lotMovementItem.setLot(existingLot);
                lotMovementItem.setStockOnHand(lotOnHand.getQuantityOnHand());
            }
        } else {
            if (existingLot == null) {
                lotMovementItem.setStockOnHand(0L);
            } else {
                lotOnHand = getLotOnHandByLot(existingLot);
                lotMovementItem.setStockOnHand(lotOnHand.getQuantityOnHand());
            }
        }
    }

    public void createOrUpdateLot(final Lot lot) throws LMISException {
        dbUtil.withDao(Lot.class, (DbUtil.Operation<Lot, Void>) dao -> {
            lot.setLotNumber(lot.getLotNumber().toUpperCase());
            dao.createOrUpdate(lot);
            return null;
        });
    }

    private void createOrUpdateLotOnHand(final LotOnHand finalLotOnHand) throws LMISException {
        dbUtil.withDao(LotOnHand.class, (DbUtil.Operation<LotOnHand, Void>) dao -> {
            dao.createOrUpdate(finalLotOnHand);
            return null;
        });
    }

    public void createLotMovementItem(final LotMovementItem lotMovementItem) throws LMISException {
        lotMovementItem.setCreatedAt(new Date());
        lotMovementItem.setUpdatedAt(new Date());

        dbUtil.withDao(LotMovementItem.class, (DbUtil.Operation<LotMovementItem, Void>) dao -> {
            dao.createOrUpdate(lotMovementItem);
            return null;
        });
    }

    public LotOnHand getLotOnHandByLot(final Lot lot) throws LMISException {
        return dbUtil.withDao(LotOnHand.class, dao -> dao.queryBuilder()
                .where()
                .eq("lot_id", lot.getId())
                .queryForFirst());
    }

    public Lot getLotByLotNumberAndProductId(final String lotNumber, final long productId) throws LMISException {
        return dbUtil.withDao(Lot.class, dao -> dao.queryBuilder()
                .where()
                .eq("lotNumber", lotNumber.toUpperCase())
                .and()
                .eq("product_id", productId)
                .queryForFirst());
    }

    public void createOrUpdateLotsInformation(final List<LotOnHand> lotOnHandListWrapper) throws LMISException {
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
                .where().eq("product_id", stockCard.getProduct() != null ? stockCard.getProduct().getId() : "null").query());
        List<LotOnHand> lotOnHands = dbUtil.withDao(LotOnHand.class, dao -> dao.queryBuilder()
                .where().eq("stockCard_id", stockCard.getId())
                .query());
        List<Long> lotIds = FluentIterable.from(lots).transform(lot -> lot.getId()).toList();
        List<LotMovementItem> lotMovementItems = dbUtil.withDao(LotMovementItem.class, dao -> dao.queryBuilder()
                .where().in("lot_id", lotIds)
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

    public void deleteLotMovementItems(List<StockMovementItem> stockMovementItems){
        List<String> stockMovementItemIds = new ArrayList<>();
        for(StockMovementItem item : stockMovementItems) {
            stockMovementItemIds.add(String.valueOf(item.getId()));
        }
        String deleteRowSql = "delete from lot_movement_items where stockMovementItem_id in ("
                + StringUtils.join(stockMovementItemIds, ",")
                + ")";
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRowSql);
    }

    public void resetLotMovementItems(Map<String,List<StockMovementItem>> stockMovementItemsMap) {
        List<StockMovementItem> keepMovements = new ArrayList<>();
        for (Map.Entry map : stockMovementItemsMap.entrySet()) {
            keepMovements.addAll(stockMovementItemsMap.get(map.getKey()));
        }
        for (StockMovementItem item : keepMovements) {
            String updateSql = "update lot_movement_items set stockOnHand = " + item.getMovementQuantity() +" "+
                    " where stockMovementItem_id =" + item.getId();
            LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(updateSql);
        }
    }
}
