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
import com.j256.ormlite.table.TableUtils;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.DraftBulkIssueProductLotItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class BulkIssueRepository {

  private final GenericDao<DraftBulkIssueProduct> draftProductGenericDao;
  private final GenericDao<DraftBulkIssueProductLotItem> draftLotGenericDao;

  @Inject
  private final DbUtil dbUtil;

  @Inject
  public BulkIssueRepository(Context context, DbUtil dbUtil) {
    this.draftProductGenericDao = new GenericDao<>(DraftBulkIssueProduct.class, context);
    this.draftLotGenericDao = new GenericDao<>(DraftBulkIssueProductLotItem.class, context);
    this.dbUtil = dbUtil;
  }

  public boolean hasDraft() throws LMISException {
    return CollectionUtils.isNotEmpty(queryAllBulkIssueDraft());
  }

  public List<DraftBulkIssueProduct> queryAllBulkIssueDraft() throws LMISException {
    return draftProductGenericDao.queryForAll();
  }

  public void deleteDraft() throws LMISException {
    dbUtil.withDaoAsBatch(DraftBulkIssueProduct.class, dao -> {
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftBulkIssueProduct.class);
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftBulkIssueProductLotItem.class);
      return null;
    });
  }

  public void saveDraft(List<DraftBulkIssueProduct> draftProducts) throws LMISException {
    if (CollectionUtils.isEmpty(draftProducts)) {
      return;
    }
    dbUtil.withDaoAsBatch(DraftBulkIssueProduct.class, dao -> {
      deleteDraft();
      for (DraftBulkIssueProduct draftProduct : draftProducts) {
        draftProductGenericDao.createOrUpdate(draftProduct);
        for (DraftBulkIssueProductLotItem lotItem : draftProduct.getDraftLotItemListWrapper()) {
          draftLotGenericDao.createOrUpdate(lotItem);
        }
      }
      return null;
    });
  }
}
