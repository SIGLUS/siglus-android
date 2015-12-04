package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class RnrFormItemRepository {
    GenericDao<RnrFormItem> genericDao;

    @Inject
    DbUtil dbUtil;


    @Inject
    public RnrFormItemRepository(Context context) {
        this.genericDao = new GenericDao<>(RnrFormItem.class, context);
    }

    public List<RnrFormItem> queryListForLowStockByProductId(final Product product) throws LMISException {
        return dbUtil.withDao(RnrFormItem.class, new DbUtil.Operation<RnrFormItem, List<RnrFormItem>>() {
            @Override
            public List<RnrFormItem> operate(Dao<RnrFormItem, String> dao) throws SQLException {
                return dao.queryBuilder().orderBy("id", false).limit(3L).where().eq("product_id", product.getId()).and().ne("inventory", 0).query();
            }
        });
    }

    public void create(final List<RnrFormItem> rnrFormItemList) throws LMISException {
        dbUtil.withDaoAsBatch(RnrFormItem.class, new DbUtil.Operation<RnrFormItem, Void>() {
            @Override
            public Void operate(Dao<RnrFormItem, String> dao) throws SQLException {
                for (RnrFormItem item : rnrFormItemList) {
                    dao.create(item);
                }
                return null;
            }
        });
    }

    public void delete(final List<RnrFormItem> rnrFormItemListWrapper) throws LMISException {
        dbUtil.withDaoAsBatch(RnrFormItem.class, new DbUtil.Operation<RnrFormItem, Void>() {
            @Override
            public Void operate(Dao<RnrFormItem, String> dao) throws SQLException {
                for (RnrFormItem item : rnrFormItemListWrapper) {
                    dao.delete(item);
                }
                return null;
            }
        });
    }

    public int getLowStockAvg(final Product product) {
        try {
            List<RnrFormItem> rnrFormItemList = queryListForLowStockByProductId(product);
            long total = 0;
            for (RnrFormItem item : rnrFormItemList) {
                total += item.getIssued();
            }
            if (rnrFormItemList.size() >= 3) {
                return (int) Math.ceil((total / rnrFormItemList.size()) * 0.05);
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return 0;
    }
}
