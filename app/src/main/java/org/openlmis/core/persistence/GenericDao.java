/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

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

    public void refresh(final Model model) throws LMISException{
        dbUtil.withDao(context, type, new DbUtil.Operation<Model, Void>() {
            @Override
            public Void operate(Dao<Model, String> dao) throws SQLException {
                dao.refresh(model);
                return null;
            }
        });
    }

    public Integer delete(final Model object) throws LMISException {
        return dbUtil.withDao(context, type, new DbUtil.Operation<Model, Integer>() {
            @Override
            public Integer operate(Dao<Model, String> dao) throws SQLException {
                return dao.delete(object);
            }
        });
    }
}