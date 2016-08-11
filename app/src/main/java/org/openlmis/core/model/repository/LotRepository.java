package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.persistence.DbUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class LotRepository {

    @Inject
    DbUtil dbUtil;
    @Inject
    Context context;

    public void batchCreateLotsAndLotMovements(final List<LotMovementItem> lotMovementItemListWrapper) throws LMISException {
        dbUtil.withDaoAsBatch(Lot.class, new DbUtil.Operation<Lot, Void>() {
            @Override
            public Void operate(Dao<Lot, String> dao) throws SQLException {
                for (LotMovementItem lotMovementItem : lotMovementItemListWrapper) {
                    Lot lot = lotMovementItem.getLot();
                    lot.setCreatedAt(new Date());
                    lot.setUpdatedAt(new Date());
                    dao.createOrUpdate(lot);
                }
                return null;
            }
        });
        dbUtil.withDaoAsBatch(LotMovementItem.class, new DbUtil.Operation<LotMovementItem, Void>() {
            @Override
            public Void operate(Dao<LotMovementItem, String> dao) throws SQLException {
                for (LotMovementItem lotMovementItem : lotMovementItemListWrapper) {
                    lotMovementItem.setCreatedAt(new Date());
                    lotMovementItem.setUpdatedAt(new Date());
                    dao.createOrUpdate(lotMovementItem);
                }
                return null;
            }
        });
    }
}
