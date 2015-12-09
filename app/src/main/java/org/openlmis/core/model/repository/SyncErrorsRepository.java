package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.persistence.GenericDao;

public class SyncErrorsRepository {

    GenericDao<SyncError> genericDao;

    @Inject
    public SyncErrorsRepository(Context context) {
        genericDao = new GenericDao<>(SyncError.class, context);
    }

    public void save(final SyncError syncError) {
        try {
            genericDao.createOrUpdate(syncError);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void delete(SyncError syncError){
        try {
            genericDao.delete(syncError);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }
}
