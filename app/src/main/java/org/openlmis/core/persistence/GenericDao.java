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

import org.openlmis.core.exceptions.LMISException;

import java.util.List;

import javax.inject.Inject;

import roboguice.RoboGuice;

public class GenericDao<Model> {
    @Inject
    DbUtil dbUtil;

    private Class<Model> type;

    private Context context;

    @Inject
    public GenericDao(Class<Model> type, Context context) {
        this.type = type;
        this.context = context;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    public Model create(final Model object) throws LMISException {
        return dbUtil.withDao(context, type, dao -> {
            dao.create(object);
            return object;
        });
    }

    public Model createOrUpdate(final Model object) throws LMISException {
        return dbUtil.withDao(context, type, dao -> {
            dao.createOrUpdate(object);
            return object;
        });
    }

    public List<Model> queryForAll() throws LMISException {
        return dbUtil.withDao(context, type, dao -> dao.queryForAll());
    }

    public Integer update(final Model object) throws LMISException {
        return dbUtil.withDao(context, type, dao -> dao.update(object));
    }

    public Model getById(final String id) throws LMISException {
        return dbUtil.withDao(context, type, dao -> dao.queryForId(id));
    }

    public void refresh(final Model model) throws LMISException {
        dbUtil.withDao(context, type, (DbUtil.Operation<Model, Void>) dao -> {
            dao.refresh(model);
            return null;
        });
    }

    public Integer delete(final Model object) throws LMISException {
        return dbUtil.withDao(context, type, dao -> dao.delete(object));
    }

    public boolean create(final List<Model> models) throws LMISException {
        return dbUtil.withDaoAsBatch(context, type, dao -> {
            for (Model model : models) {
                dao.createOrUpdate(model);
            }
            return true;
        });
    }

}