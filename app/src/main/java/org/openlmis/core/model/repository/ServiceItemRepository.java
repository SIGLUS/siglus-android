package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class ServiceItemRepository {

    GenericDao<ServiceItem> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public ServiceItemRepository(Context context) {
        this.genericDao = new GenericDao<>(ServiceItem.class, context);
    }

    public void batchCreateOrUpdate(final List<ServiceItem> serviceItemList) throws LMISException {
        dbUtil.withDaoAsBatch(ServiceItem.class, new DbUtil.Operation<ServiceItem, Void>() {
            @Override
            public Void operate(Dao<ServiceItem, String> dao) throws SQLException, LMISException {
                for (ServiceItem serviceItem : serviceItemList) {
                    dao.createOrUpdate(serviceItem);
                }
                return null;
            }
        });
    }

    public void deleteServiceItems(final List<ServiceItem> serviceItems) throws LMISException {
        dbUtil.withDaoAsBatch(ServiceItem.class, new DbUtil.Operation<ServiceItem, Void>() {
            @Override
            public Void operate(Dao<ServiceItem, String> dao) throws SQLException, LMISException {
                dao.delete(serviceItems);
                return null;
            }
        });
    }

}
