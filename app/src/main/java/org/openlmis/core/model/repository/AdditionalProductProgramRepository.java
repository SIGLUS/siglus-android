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
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.AdditionalProductProgram;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class AdditionalProductProgramRepository {

  private final GenericDao<AdditionalProductProgram> genericDao;

  @Inject
  private final DbUtil dbUtil;

  private final Context context;

  @Inject
  public AdditionalProductProgramRepository(DbUtil dbUtil, Context context) {
    this.genericDao = new GenericDao<>(AdditionalProductProgram.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public AdditionalProductProgram queryByProgramCodeAndProductCode(String programCode, String productCode)
      throws LMISException {
    return dbUtil.withDao(AdditionalProductProgram.class,
        dao -> dao.queryBuilder()
            .where()
            .eq("programCode", programCode)
            .and()
            .eq("productCode", productCode)
            .queryForFirst());
  }

  public void createOrUpdate(AdditionalProductProgram additionalProductProgram) throws LMISException {
    AdditionalProductProgram existAdditionalProductProgram = queryByProgramCodeAndProductCode(
        additionalProductProgram.getProgramCode(), additionalProductProgram.getProductCode());
    if (existAdditionalProductProgram != null) {
      additionalProductProgram.setId(existAdditionalProductProgram.getId());
    }
    genericDao.createOrUpdate(additionalProductProgram);
  }
}
