package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.utils.Constants;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.List;

public class RnrFormItemRepository {
    GenericDao<RnrFormItem> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    private ProgramRepository programRepository;


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

    public void batchCreateOrUpdate(final List<RnrFormItem> rnrFormItemList) throws LMISException {
        dbUtil.withDaoAsBatch(RnrFormItem.class, new DbUtil.Operation<RnrFormItem, Void>() {
            @Override
            public Void operate(Dao<RnrFormItem, String> dao) throws SQLException {
                for (RnrFormItem item : rnrFormItemList) {
                    dao.createOrUpdate(item);
                }
                return null;
            }
        });
    }

    public void deleteFormItems(final List<RnrFormItem> rnrFormItemListWrapper) throws LMISException {
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

    public List<Long> listAllProductIdsInCurrentVIADraft() throws LMISException {
        final List<Long> programIds = programRepository.queryProgramIdsByProgramCodeOrParentCode(Constants.VIA_PROGRAM_CODE);
        final RnRForm rnRForm = dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, RnRForm>() {
            @Override
            public RnRForm operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where()
                        .in("program_id", programIds)
                        .and().eq("status", RnRForm.STATUS.DRAFT)
                        .and().eq("emergency", false).queryForFirst();
            }
        });

        List<RnrFormItem> rnrFormItems = dbUtil.withDao(RnrFormItem.class, new DbUtil.Operation<RnrFormItem, List<RnrFormItem>>() {
            @Override
            public List<RnrFormItem> operate(Dao<RnrFormItem, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("form_id", rnRForm.getId()).query();
            }
        });

        return FluentIterable.from(rnrFormItems).transform(new Function<RnrFormItem, Long>() {
            @Override
            public Long apply(RnrFormItem rnrFormItem) {
                return rnrFormItem.getProduct().getId();
            }
        }).toList();
    }

}
