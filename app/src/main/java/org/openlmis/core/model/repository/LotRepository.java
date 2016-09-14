package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class LotRepository {

    @Inject
    DbUtil dbUtil;
    @Inject
    Context context;

    public void batchCreateLotsAndLotMovements(final List<LotMovementItem> lotMovementItemListWrapper) throws LMISException {
        dbUtil.withDaoAsBatch(LotMovementItem.class, new DbUtil.Operation<LotMovementItem, Object>() {
            @Override
            public LotMovementItem operate(Dao<LotMovementItem, String> dao) throws SQLException, LMISException {
                for (final LotMovementItem lotMovementItem : lotMovementItemListWrapper) {
                    createOrUpdateLotAndLotOnHand(lotMovementItem);
                    createLotMovementItem(lotMovementItem);
                }
                return null;
            }
        });
    }

    private void createOrUpdateLotAndLotOnHand(LotMovementItem lotMovementItem) throws LMISException {
        final Lot lot = lotMovementItem.getLot();
        Lot existingLot = getLotByLotNumberAndProductId(lot.getLotNumber(), lot.getProduct().getId());
        LotOnHand lotOnHand;

        if (existingLot == null) {
            lot.setCreatedAt(new Date());
            lot.setUpdatedAt(new Date());
            createOrUpdateLot(lot);

            lotOnHand = new LotOnHand(lot, lotMovementItem.getStockMovementItem().getStockCard(), lotMovementItem.getMovementQuantity());
            createOrUpdateLotOnHand(lotOnHand);

            lotMovementItem.setStockOnHand(lotMovementItem.getMovementQuantity());
        } else {
            lotOnHand = getLotOnHandByLot(existingLot);
            if (lotMovementItem.isStockOnHandCalculated()) {
                lotOnHand.setQuantityOnHand(lotMovementItem.getStockOnHand());
            } else {
                lotOnHand.setQuantityOnHand(lotOnHand.getQuantityOnHand() + lotMovementItem.getMovementQuantity());
            }
            createOrUpdateLotOnHand(lotOnHand);

            lotMovementItem.setLot(existingLot);
            lotMovementItem.setStockOnHand(lotOnHand.getQuantityOnHand());
        }
    }

    public void createOrUpdateLot(final Lot lot) throws LMISException {
        dbUtil.withDao(Lot.class, new DbUtil.Operation<Lot, Void>() {
            @Override
            public Void operate(Dao<Lot, String> dao) throws SQLException {
                lot.setLotNumber(lot.getLotNumber().toUpperCase());
                dao.createOrUpdate(lot);
                return null;
            }
        });
    }

    private void createOrUpdateLotOnHand(final LotOnHand finalLotOnHand) throws LMISException {
        dbUtil.withDao(LotOnHand.class, new DbUtil.Operation<LotOnHand, Void>() {
            @Override
            public Void operate(Dao<LotOnHand, String> dao) throws SQLException {
                dao.createOrUpdate(finalLotOnHand);
                return null;
            }
        });
    }

    public void createLotMovementItem(final LotMovementItem lotMovementItem) throws LMISException {
        lotMovementItem.setCreatedAt(new Date());
        lotMovementItem.setUpdatedAt(new Date());

        dbUtil.withDao(LotMovementItem.class, new DbUtil.Operation<LotMovementItem, Void>() {
            @Override
            public Void operate(Dao<LotMovementItem, String> dao) throws SQLException {
                dao.createOrUpdate(lotMovementItem);
                return null;
            }
        });
    }

    public LotOnHand getLotOnHandByLot(final Lot lot) throws LMISException {
        return dbUtil.withDao(LotOnHand.class, new DbUtil.Operation<LotOnHand, LotOnHand>() {
            @Override
            public LotOnHand operate(Dao<LotOnHand, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .where()
                        .eq("lot_id", lot.getId())
                        .queryForFirst();
            }
        });
    }

    public Lot getLotByLotNumberAndProductId(final String lotNumber, final long productId) throws LMISException {
        return dbUtil.withDao(Lot.class, new DbUtil.Operation<Lot, Lot>() {
            @Override
            public Lot operate(Dao<Lot, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .where()
                        .eq("lotNumber", lotNumber.toUpperCase())
                        .and()
                        .eq("product_id", productId)
                        .queryForFirst();
            }
        });
    }

    public void createOrUpdateLotsInformation(final List<LotOnHand> lotOnHandListWrapper) throws LMISException {
        dbUtil.withDaoAsBatch(LotOnHand.class, new DbUtil.Operation<LotOnHand, Object>() {
            @Override
            public LotOnHand operate(Dao<LotOnHand, String> dao) throws SQLException, LMISException {
                for (final LotOnHand lotOnHand : lotOnHandListWrapper) {
                    createOrUpdateLot(lotOnHand.getLot());
                    createOrUpdateLotOnHand(lotOnHand);
                }
                return null;
            }
        });
    }

    public List<Lot> queryAllLot() {
        try {
            return new GenericDao<>(Lot.class, context).queryForAll();
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return null;
    }
}
