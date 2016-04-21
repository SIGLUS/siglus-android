package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
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
        return dbUtil.withDao(RnRFormSignature.class, new DbUtil.Operation<RnRFormSignature, List<RnRFormSignature>>() {
            @Override
            public List<RnRFormSignature> operate(Dao<RnRFormSignature, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("form_id", formId).query();
            }
        });
    }

    public void batchCreateOrUpdate(final List<RnRFormSignature> signatures) throws LMISException {
        dbUtil.withDaoAsBatch(RnRFormSignature.class, new DbUtil.Operation<RnRFormSignature, Void>() {
            @Override
            public Void operate(Dao<RnRFormSignature, String> dao) throws SQLException {
                for (RnRFormSignature item : signatures) {
                    dao.createOrUpdate(item);
                }
                return null;
            }
        });
    }

    public void batchDelete(final List<RnRFormSignature> signatures) throws LMISException {
        dbUtil.withDaoAsBatch(RnRFormSignature.class, new DbUtil.Operation<RnRFormSignature, Void>() {
            @Override
            public Void operate(Dao<RnRFormSignature, String> dao) throws SQLException {
                dao.delete(signatures);
                return null;
            }
        });
    }
}
