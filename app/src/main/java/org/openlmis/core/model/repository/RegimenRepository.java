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
import static org.openlmis.core.constant.FieldConstants.IS_CUSTOM;
import static org.openlmis.core.constant.FieldConstants.NAME;
import static org.openlmis.core.constant.FieldConstants.PROGRAM_ID;
import static org.openlmis.core.constant.FieldConstants.TYPE;

import android.content.Context;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import java.sql.SQLException;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class RegimenRepository {

  GenericDao<Regimen> regimenGenericDao;

  @Inject
  ProgramRepository programRepository;

  @Inject
  DbUtil dbUtil;

  @Inject
  Context context;

  @Inject
  public RegimenRepository(Context context) {
    this.regimenGenericDao = new GenericDao<>(Regimen.class, context);
  }

  public Regimen getByNameAndCategory(final String name, final Regimen.RegimeType category)
      throws LMISException {
    return dbUtil.withDao(Regimen.class,
        dao -> dao.queryBuilder().where().eq(NAME, name).and().eq(TYPE, category)
            .queryForFirst());
  }

  public Regimen getByCode(final String code) throws LMISException {
    return dbUtil
        .withDao(Regimen.class, dao -> dao.queryBuilder().where().eq(CODE, code).queryForFirst());
  }

  public void batchSave(List<Regimen> regimens) throws LMISException {
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (Regimen regimen : regimens) {
          if (!regimen.isActive()) {
            continue;
          }
          createOrUpdate(regimen);
        }
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e, "RegimenRepository.batchSave");
    }
  }

  private void createOrUpdate(Regimen regimen) throws LMISException {
    Regimen existingRegimen = getByCode(regimen.getCode());
    if (existingRegimen == null) {
      create(regimen);
    } else {
      regimen.setId(existingRegimen.getId());
      regimenGenericDao.update(regimen);
    }
  }

  public void create(Regimen regimen) throws LMISException {
    regimenGenericDao.create(regimen);
  }

  public List<Regimen> listDefaultRegime(String programCode) throws LMISException {
    return dbUtil.withDao(Regimen.class, dao -> dao.queryBuilder()
        .where()
        .eq(IS_CUSTOM, false)
        .and().eq(ACTIVE, true)
        .and().eq(PROGRAM_ID, programRepository.queryByCode(programCode).getId())
        .query());
  }

  public List<Regimen> listNonCustomRegimen(String programCode, Regimen.RegimeType type) throws LMISException {
    return dbUtil.withDao(Regimen.class, dao -> dao.queryBuilder()
        .where()
        .eq(IS_CUSTOM, true)
        .and().eq(ACTIVE, true)
        .and().eq(TYPE, type)
        .and().eq(PROGRAM_ID, programRepository.queryByCode(programCode).getId())
        .query());
  }

}
