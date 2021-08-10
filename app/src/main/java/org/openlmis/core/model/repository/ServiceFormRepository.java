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

package org.openlmis.core.model.repository;

import static org.openlmis.core.constant.FieldConstants.ACTIVE;
import static org.openlmis.core.constant.FieldConstants.CODE;
import static org.openlmis.core.constant.FieldConstants.PROGRAM_ID;

import android.content.Context;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Service;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class ServiceFormRepository {

  GenericDao<Service> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  public ServiceFormRepository(Context context) {
    genericDao = new GenericDao<>(Service.class, context);
  }

  public void batchCreateOrUpdateServiceList(final List<Service> serviceList) throws LMISException {
    dbUtil.withDaoAsBatch(Service.class, dao -> {
      for (Service service : serviceList) {
        createOrUpdate(service);
      }
      return null;
    });
  }

  public void createOrUpdate(Service service) throws LMISException {
    Service existingService = queryByCode(service.getCode());
    if (existingService == null) {
      genericDao.create(service);
    } else {
      service.setId(existingService.getId());
      genericDao.update(service);
    }
  }

  public Service queryByCode(final String serviceCode) throws LMISException {
    return dbUtil.withDao(Service.class, dao -> dao.queryBuilder().where().eq(CODE, serviceCode).queryForFirst());
  }

  protected List<Service> listAllActive() throws LMISException {
    return dbUtil.withDao(Service.class, dao ->
        dao.queryBuilder().where().eq(ACTIVE, true).query());
  }

  public List<Service> listAllActiveWithProgram(Program program) throws LMISException {
    return dbUtil.withDao(Service.class, dao ->
        dao.queryBuilder()
            .where().eq(ACTIVE, true)
            .and().eq(PROGRAM_ID, program.getId())
            .query());
  }
}
