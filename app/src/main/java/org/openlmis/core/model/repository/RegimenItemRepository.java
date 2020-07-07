package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

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
        dbUtil.withDaoAsBatch(RegimenItem.class, (DbUtil.Operation<RegimenItem, Void>) dao -> {
            for (RegimenItem item : regimenItemList) {
                dao.createOrUpdate(item);
            }
            return null;
        });
    }

    public void create(final RegimenItem regimenItem) throws LMISException {
        dbUtil.withDao(RegimenItem.class, (DbUtil.Operation<RegimenItem, Void>) dao -> {
            dao.create(regimenItem);
            return null;
        });
    }

    public void deleteRegimenItems(final List<RegimenItem> regimenItemListWrapper) throws LMISException {
        dbUtil.withDao(RegimenItem.class, (DbUtil.Operation<RegimenItem, Void>) dao -> {
            dao.delete(regimenItemListWrapper);
            return null;
        });
    }

    public void deleteRegimeItem(final RegimenItem item) throws LMISException {
        dbUtil.withDao(RegimenItem.class, (DbUtil.Operation<RegimenItem, Void>) dao -> {
            dao.delete(item);
            return null;
        });
    }

    public List<RegimenItem> listAll() throws LMISException {
        return dbUtil.withDao(RegimenItem.class, dao -> dao.queryForAll());
    }

}
