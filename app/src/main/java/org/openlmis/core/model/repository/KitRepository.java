package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Kit;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;

public class KitRepository {
    GenericDao<Kit> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public KitRepository(Context context) {
        genericDao = new GenericDao<>(Kit.class, context);
    }

    public Kit getById(final long id) throws LMISException {
        return dbUtil.withDao(Kit.class, new DbUtil.Operation<Kit, Kit>() {
            @Override
            public Kit operate(Dao<Kit, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("id", id).and().eq("isKit", true).queryForFirst();
            }
        });
    }

    public Kit getByCode(final String code) throws LMISException {
        return dbUtil.withDao(Kit.class, new DbUtil.Operation<Kit, Kit>() {
            @Override
            public Kit operate(Dao<Kit, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("code", code).and().eq("isKit", true).queryForFirst();
            }
        });
    }
}
