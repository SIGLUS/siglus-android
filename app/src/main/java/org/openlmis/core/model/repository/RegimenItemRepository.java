package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class RegimenItemRepository {

    GenericDao<RegimenItem> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public RegimenItemRepository(Context context) {
        this.genericDao = new GenericDao<>(RegimenItem.class, context);
    }

    public void batchCreateOrUpdate(final List<RegimenItem> regimenItemList) throws LMISException {
        dbUtil.withDaoAsBatch(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                for (RegimenItem item : regimenItemList) {
                    dao.createOrUpdate(item);
                }
                return null;
            }
        });
    }

    public void create(final RegimenItem regimenItem) throws LMISException {
        dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                dao.create(regimenItem);
                return null;
            }
        });
    }

    public void deleteRegimenItems(final List<RegimenItem> regimenItemListWrapper) throws LMISException {
        dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                dao.delete(regimenItemListWrapper);
                return null;
            }
        });
    }

    public void deleteRegimeItem(final RegimenItem item) throws LMISException {
        dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                dao.delete(item);
                return null;
            }
        });
    }

    public List<RegimenItem> listAll() throws LMISException {
        return dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, List<RegimenItem>>() {
            @Override
            public List<RegimenItem> operate(Dao<RegimenItem, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }

}
