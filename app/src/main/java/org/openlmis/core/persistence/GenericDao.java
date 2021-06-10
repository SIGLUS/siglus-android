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
import java.util.List;
import javax.inject.Inject;
import org.openlmis.core.exceptions.LMISException;
import roboguice.RoboGuice;

public class GenericDao<T> {

  @Inject
  DbUtil dbUtil;

  private final Class<T> type;

  private final Context context;

  @Inject
  public GenericDao(Class<T> type, Context context) {
    this.type = type;
    this.context = context;
    RoboGuice.getInjector(context).injectMembers(this);
  }

  public T create(final T object) throws LMISException {
    return dbUtil.withDao(context, type, dao -> {
      dao.create(object);
      return object;
    });
  }

  public boolean create(final List<T> models) throws LMISException {
    return dbUtil.withDaoAsBatch(context, type, dao -> {
      for (T model : models) {
        dao.createOrUpdate(model);
      }
      return true;
    });
  }

  public T createOrUpdate(final T object) throws LMISException {
    return dbUtil.withDao(context, type, dao -> {
      dao.createOrUpdate(object);
      return object;
    });
  }

  public List<T> queryForAll() throws LMISException {
    return dbUtil.withDao(context, type, dao -> dao.queryForAll());
  }

  public Integer update(final T object) throws LMISException {
    return dbUtil.withDao(context, type, dao -> dao.update(object));
  }

  public T getById(final String id) throws LMISException {
    return dbUtil.withDao(context, type, dao -> dao.queryForId(id));
  }

  public void refresh(final T model) throws LMISException {
    dbUtil.withDao(context, type, (DbUtil.Operation<T, Void>) dao -> {
      dao.refresh(model);
      return null;
    });
  }

  public Integer delete(final T object) throws LMISException {
    return dbUtil.withDao(context, type, dao -> dao.delete(object));
  }

}