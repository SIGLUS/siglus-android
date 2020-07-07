package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class RnrFormItemRepository {
    GenericDao<RnrFormItem> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    private ServiceItemRepository serviceItemRepository;


    @Inject
    public RnrFormItemRepository(Context context) {
        this.genericDao = new GenericDao<>(RnrFormItem.class, context);
    }

    public List<RnrFormItem> queryListForLowStockByProductId(final Product product) throws LMISException {
        return dbUtil.withDao(RnrFormItem.class, dao -> dao.queryBuilder().orderBy("id", false).limit(3L).where().eq("product_id", product.getId()).and().ne("inventory", 0).query());
    }

    public void batchCreateOrUpdate(final List<RnrFormItem> rnrFormItemList) throws LMISException {
        batchCreateOrUpdateItems(rnrFormItemList);
        for (RnrFormItem item : rnrFormItemList) {
            List<ServiceItem> serviceItems = item.getServiceItemListWrapper();
            if (serviceItems.size() > 0) {
                serviceItemRepository.batchCreateOrUpdate(serviceItems);
            }
        }
    }

    private void batchCreateOrUpdateItems(final List<RnrFormItem> rnrFormItemList) throws LMISException {
        dbUtil.withDaoAsBatch(RnrFormItem.class, (DbUtil.Operation<RnrFormItem, Void>) dao -> {
            for (RnrFormItem item : rnrFormItemList) {
                dao.createOrUpdate(item);
            }
            return null;
        });
    }

    public void deleteFormItems(final List<RnrFormItem> rnrFormItemListWrapper) throws LMISException {
        batchDeleteItems(rnrFormItemListWrapper);

        for (RnrFormItem item : rnrFormItemListWrapper) {
            List<ServiceItem> serviceItems = item.getServiceItemListWrapper();
            if (serviceItems.size() > 0) {
                serviceItemRepository.deleteServiceItems(serviceItems);
            }
        }
    }

    private void batchDeleteItems(final List<RnrFormItem> rnrFormItemListWrapper) throws LMISException {
        dbUtil.withDaoAsBatch(RnrFormItem.class, (DbUtil.Operation<RnrFormItem, Void>) dao -> {
            for (RnrFormItem item : rnrFormItemListWrapper) {
                dao.delete(item);
            }
            return null;
        });
    }

    public List<RnrFormItem> listAllNewRnrItems() throws LMISException {
        return dbUtil.withDao(RnrFormItem.class, dao -> dao.queryBuilder().where().eq("isManualAdd", true).query());
    }

    public void deleteRnrItem(final RnrFormItem rnrFormItem) throws LMISException {
        dbUtil.withDaoAsBatch(RnrFormItem.class, (DbUtil.Operation<RnrFormItem, Void>) dao -> {
            dao.delete(rnrFormItem);
            return null;
        });
    }

}
