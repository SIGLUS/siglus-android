package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

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
        dbUtil.withDaoAsBatch(ServiceItem.class, (DbUtil.Operation<ServiceItem, Void>) dao -> {
            for (ServiceItem serviceItem : serviceItemList) {
                dao.createOrUpdate(serviceItem);
            }
            return null;
        });
    }

    public void deleteServiceItems(final List<ServiceItem> serviceItems) throws LMISException {
        dbUtil.withDaoAsBatch(ServiceItem.class, (DbUtil.Operation<ServiceItem, Void>) dao -> {
            dao.delete(serviceItems);
            return null;
        });
    }

}
