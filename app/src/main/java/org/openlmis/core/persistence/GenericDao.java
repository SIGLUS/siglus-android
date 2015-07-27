package org.openlmis.core.persistence;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import roboguice.RoboGuice;

public class GenericDao<Model> {
    @Inject
    DbUtil dbUtil;

    private Class<Model> type;

    private Context context;

    public GenericDao(Class<Model> type, Context context) {
        this.type = type;
        this.context = context;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    public Model create(final Model object) throws LMISException {
        return dbUtil.withDao(context, type, new DbUtil.Operation<Model, Model>() {
            @Override
            public Model operate(Dao<Model, String> dao) throws SQLException {
                dao.create(object);
                return object;
            }
        });
    }

    public Model createOrUpdate(final Model object) throws LMISException {
        return dbUtil.withDao(context, type, new DbUtil.Operation<Model, Model>() {
            @Override
            public Model operate(Dao<Model, String> dao) throws SQLException {
                dao.createOrUpdate(object);
                return object;
            }
        });
    }

    public List<Model> queryForAll() throws LMISException {
        return dbUtil.withDao(context, type, new DbUtil.Operation<Model, List<Model>>() {
            @Override
            public List<Model> operate(Dao<Model, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }

    public Integer update(final Model object) throws LMISException {
        return dbUtil.withDao(context, type, new DbUtil.Operation<Model, Integer>() {
            @Override
            public Integer operate(Dao<Model, String> dao) throws SQLException {
                return dao.update(object);
            }
        });
    }

    public long countOf() throws LMISException {
        return dbUtil.withDao(context, type, new DbUtil.Operation<Model, Long>() {
            @Override
            public Long operate(Dao<Model, String> dao) throws SQLException {
                return dao.countOf();
            }
        });
    }

    public Model getById(final String id) throws LMISException {
        return dbUtil.withDao(context, type, new DbUtil.Operation<Model, Model>() {
            @Override
            public Model operate(Dao<Model, String> dao) throws SQLException {
                return dao.queryForId(id);
            }
        });
    }

    public void bulkOperation(DbUtil.Operation<Model, Object> operation) throws LMISException {
        dbUtil.withDaoAsBatch(context, type, operation);
    }
}