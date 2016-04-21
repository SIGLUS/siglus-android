package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class BaseInfoItemRepository {

    GenericDao<BaseInfoItem> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public BaseInfoItemRepository(Context context) {
        this.genericDao = new GenericDao<>(BaseInfoItem.class, context);
    }

    public void batchCreateOrUpdate(final List<BaseInfoItem> baseInfoItemList) throws LMISException {
        dbUtil.withDaoAsBatch(BaseInfoItem.class, new DbUtil.Operation<BaseInfoItem, Void>() {
            @Override
            public Void operate(Dao<BaseInfoItem, String> dao) throws SQLException {
                for (BaseInfoItem item : baseInfoItemList) {
                    dao.createOrUpdate(item);
                }
                return null;
            }
        });
    }

    public void batchDelete(final List<BaseInfoItem> baseInfoItemListWrapper) throws LMISException {
        dbUtil.withDaoAsBatch(BaseInfoItem.class, new DbUtil.Operation<BaseInfoItem, Void>() {
            @Override
            public Void operate(Dao<BaseInfoItem, String> dao) throws SQLException {
                for (BaseInfoItem item : baseInfoItemListWrapper) {
                    dao.delete(item);
                }
                return null;
            }
        });
    }
}
