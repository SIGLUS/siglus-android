package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class RnrFormSignatureRepository {
    GenericDao<RnRFormSignature> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public RnrFormSignatureRepository(Context context) {
        this.genericDao = new GenericDao<>(RnRFormSignature.class, context);
    }

    public List<RnRFormSignature> queryByRnrFormId(final long formId) throws LMISException {
        return dbUtil.withDao(RnRFormSignature.class, dao -> dao.queryBuilder().where().eq("form_id", formId).query());
    }

    public void batchCreateOrUpdate(final List<RnRFormSignature> signatures) throws LMISException {
        dbUtil.withDaoAsBatch(RnRFormSignature.class, (DbUtil.Operation<RnRFormSignature, Void>) dao -> {
            for (RnRFormSignature item : signatures) {
                dao.createOrUpdate(item);
            }
            return null;
        });
    }

    public void batchDelete(final List<RnRFormSignature> signatures) throws LMISException {
        dbUtil.withDaoAsBatch(RnRFormSignature.class, (DbUtil.Operation<RnRFormSignature, Void>) dao -> {
            dao.delete(signatures);
            return null;
        });
    }
}
