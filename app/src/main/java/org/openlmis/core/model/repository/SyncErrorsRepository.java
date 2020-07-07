package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class SyncErrorsRepository {

    final Context context;
    GenericDao<SyncError> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public SyncErrorsRepository(Context context) {
        genericDao = new GenericDao<>(SyncError.class, context);
        this.context = context;
    }

    public void save(final SyncError syncError) {
        try {
            genericDao.createOrUpdate(syncError);
        } catch (LMISException e) {
            new LMISException(e,"SyncErrorsRepository.save").reportToFabric();
        }
    }

    public void delete(SyncError syncError) {
        try {
            genericDao.delete(syncError);
        } catch (LMISException e) {
            new LMISException(e,"SyncErrorsRepository.delete").reportToFabric();
        }
    }

    public List<SyncError> getBySyncTypeAndObjectId(final SyncType syncType, final long syncObjectId) {
        try {
            return dbUtil.withDao(SyncError.class, dao -> dao.queryBuilder()
                    .where().eq("syncType", syncType)
                    .and().eq("syncObjectId", syncObjectId).query());
        } catch (LMISException e) {
            new LMISException(e,"SyncErrorsRepository.getBy").reportToFabric();
            return null;
        }
    }

    public Integer deleteBySyncTypeAndObjectId(final SyncType syncType, final long syncObjectId) {
        try {
            return dbUtil.withDao(SyncError.class, dao -> dao.delete(dao.queryBuilder()
                    .orderBy("id", false)
                    .where().eq("syncType", syncType)
                    .and().eq("syncObjectId", syncObjectId).query()));
        } catch (LMISException e) {
            new LMISException(e,"SyncErrorsRepository.deleteBy").reportToFabric();
            return null;
        }
    }

    public boolean hasSyncErrorOf(final SyncType syncType) throws LMISException {
        SyncError syncError = dbUtil.withDao(SyncError.class, dao -> dao.queryBuilder().where().eq("syncType", syncType).queryForFirst());
        return syncError != null;
    }
}
