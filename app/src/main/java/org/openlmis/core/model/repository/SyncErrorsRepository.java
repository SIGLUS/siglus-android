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
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class SyncErrorsRepository {

  final Context context;
  GenericDao<SyncError> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  public SyncErrorsRepository(Context context) {
    genericDao = new GenericDao<>(SyncError.class, context);
    this.context = context;
  }

  public void save(final SyncError syncError) {
    try {
      genericDao.createOrUpdate(syncError);
    } catch (LMISException e) {
      new LMISException(e, "SyncErrorsRepository.save").reportToFabric();
    }
  }

  public void delete(SyncError syncError) {
    try {
      genericDao.delete(syncError);
    } catch (LMISException e) {
      new LMISException(e, "SyncErrorsRepository.delete").reportToFabric();
    }
  }

  public List<SyncError> getBySyncTypeAndObjectId(final SyncType syncType,
      final long syncObjectId) {
    try {
      return dbUtil.withDao(SyncError.class, dao -> dao.queryBuilder()
          .where().eq("syncType", syncType)
          .and().eq("syncObjectId", syncObjectId).query());
    } catch (LMISException e) {
      new LMISException(e, "SyncErrorsRepository.getBy").reportToFabric();
      return null;
    }
  }

  public Integer deleteBySyncTypeAndObjectId(final SyncType syncType, final long syncObjectId) {
    try {
      return dbUtil.withDao(SyncError.class, dao -> dao.delete(dao.queryBuilder()
          .orderBy("id", false)
          .where().eq("syncType", syncType)
          .and().eq("syncObjectId", syncObjectId).query()));
    } catch (LMISException e) {
      new LMISException(e, "SyncErrorsRepository.deleteBy").reportToFabric();
      return null;
    }
  }

  public boolean hasSyncErrorOf(final SyncType syncType) throws LMISException {
    SyncError syncError = dbUtil.withDao(SyncError.class,
        dao -> dao.queryBuilder().where().eq("syncType", syncType).queryForFirst());
    return syncError != null;
  }
}
