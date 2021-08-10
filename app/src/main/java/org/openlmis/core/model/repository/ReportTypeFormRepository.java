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

import android.content.Context;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class ReportTypeFormRepository {

  GenericDao<ReportTypeForm> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  public ReportTypeFormRepository(Context context) {
    genericDao = new GenericDao<>(ReportTypeForm.class, context);
  }

  public void batchCreateOrUpdateReportTypes(final List<ReportTypeForm> reportTypeForms) throws LMISException {
    if (reportTypeForms == null) {
      return;
    }
    dbUtil.withDaoAsBatch(ReportTypeForm.class, dao -> {
      for (ReportTypeForm reportTypeForm : reportTypeForms) {
        createOrUpdate(reportTypeForm);
      }
      return null;
    });
  }

  public void createOrUpdate(ReportTypeForm reportTypeForm) throws LMISException {
    ReportTypeForm existingReportType = queryByCode(reportTypeForm.getCode());
    if (existingReportType == null) {
      genericDao.create(reportTypeForm);
    } else {
      reportTypeForm.setId(existingReportType.getId());
      genericDao.update(reportTypeForm);
    }
  }

  public ReportTypeForm queryByCode(final String code) throws LMISException {
    return dbUtil
        .withDao(ReportTypeForm.class, dao -> dao.queryBuilder().where().eq(FieldConstants.CODE, code).queryForFirst());
  }

  public List<ReportTypeForm> listAll() throws LMISException {
    return genericDao.queryForAll();
  }

  public ReportTypeForm getReportType(final String programCode) throws LMISException {
    return queryByCode(programCode);
  }

  public List<ReportTypeForm> listAllWithActive() {
    try {
      return dbUtil
          .withDao(ReportTypeForm.class, dao -> dao.queryBuilder().where().eq(FieldConstants.ACTIVE, true).query());
    } catch (LMISException e) {
      e.reportToFabric();
      return Collections.emptyList();
    }
  }
}
