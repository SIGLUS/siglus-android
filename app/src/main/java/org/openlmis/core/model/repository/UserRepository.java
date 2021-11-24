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
import android.util.Log;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.User;
import org.openlmis.core.network.ProgramCacheManager;
import org.openlmis.core.network.model.FacilityInfoResponse;
import org.openlmis.core.network.model.SupportedProgram;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class UserRepository {

  @Inject
  DbUtil dbUtil;

  @Inject
  ProgramRepository programRepository;

  @Inject
  ReportTypeFormRepository reportTypeFormRepository;

  GenericDao<User> genericDao;

  private final Context context;

  @Inject
  public UserRepository(Context context) {
    genericDao = new GenericDao<>(User.class, context);
    this.context = context;
  }

  public User mapUserFromLocal(final User user) {
    try {
      User userQueried = dbUtil.withDao(User.class,
          dao -> dao.queryBuilder().where().eq("username", user.getUsername()).and()
              .eq("password", user.getPasswordMD5()).queryForFirst());
      if (userQueried != null) {
        userQueried.setPassword(user.getPassword());
      }
      return userQueried;
    } catch (LMISException e) {
      new LMISException(e, "UserRepository.mapUserFromLocal").reportToFabric();
    }
    return null;
  }

  public void saveFacilityInfo(FacilityInfoResponse facilityInfoResponse) throws LMISException {
    User user = UserInfoMgr.getInstance().getUser();
    user.setFacilityCode(facilityInfoResponse.getCode());
    user.setFacilityName(facilityInfoResponse.getName());
    UserInfoMgr.getInstance().setUser(user);
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        createOrUpdate(user);
        List<Program> programs = covertFacilityInfoToProgram(facilityInfoResponse);
        ProgramCacheManager.addPrograms(programs);
        programRepository.batchCreateOrUpdatePrograms(programs);
        reportTypeFormRepository.batchCreateOrUpdateReportTypes(facilityInfoResponse.getSupportedReportTypes());
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  public void createOrUpdate(final User user) {
    try {
      List<User> userByUsername = getUserByUsername(user.getUsername());
      if (userByUsername.isEmpty()) {
        genericDao.create(user);
      } else {
        user.setId(userByUsername.get(userByUsername.size() - 1).getId());
        genericDao.update(user);
      }
    } catch (LMISException e) {
      new LMISException(e, "UserRepository.createOrUpdate").reportToFabric();
    }
  }

  protected List<User> getUserByUsername(final String userName) throws LMISException {
    return dbUtil
        .withDao(User.class, dao -> dao.queryBuilder().where().eq("username", userName).query());
  }

  public User getLocalUser() {
    User user = null;
    try {
      user = dbUtil.withDao(User.class, dao -> dao.queryBuilder().queryForFirst());
    } catch (LMISException e) {
      Log.w("UserRepository", e);
      new LMISException(e, "UserRepository.getLocalUser").reportToFabric();
    }
    return user;
  }

  public void deleteLocalUser() {
    try {
      dbUtil.withDao(User.class, dao -> dao.deleteBuilder().delete());
    } catch (LMISException e) {
      Log.w("UserRepository", e);
      new LMISException(e, "UserReposirory.deleteLocalUser").reportToFabric();
    }
  }

  private List<Program> covertFacilityInfoToProgram(FacilityInfoResponse facilityInfoResponse) {
    List<Program> programs = new ArrayList<>();
    for (SupportedProgram supportedProgram : facilityInfoResponse.getSupportedPrograms()) {
      Program program = Program
          .builder()
          .programCode(supportedProgram.getCode())
          .programName(supportedProgram.getName())
          .isSupportEmergency(supportedProgram.getCode().equals("VC"))
          .build();
      programs.add(program);
    }
    return programs;
  }
}
