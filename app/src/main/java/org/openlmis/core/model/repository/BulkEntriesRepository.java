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
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkEntriesProduct;
import org.openlmis.core.model.DraftBulkEntriesProductLotItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class BulkEntriesRepository {

  private final GenericDao<DraftBulkEntriesProduct> productGenericDao;
  private final GenericDao<DraftBulkEntriesProductLotItem> productLotItemGenericDao;

  @Inject
  private final DbUtil dbUtil;

  @Inject
  public BulkEntriesRepository(Context context, DbUtil dbUtil) {
    this.productGenericDao = new GenericDao<>(DraftBulkEntriesProduct.class, context);
    this.productLotItemGenericDao = new GenericDao<>(DraftBulkEntriesProductLotItem.class, context);
    this.dbUtil = dbUtil;
  }

  public void createBulkEntriesProductDraft(final DraftBulkEntriesProduct draftBulkEntriesProduct)
      throws LMISException {
    dbUtil.withDaoAsBatch(DraftBulkEntriesProduct.class, dao -> {
      productGenericDao.createOrUpdate(draftBulkEntriesProduct);
      for (DraftBulkEntriesProductLotItem draftBulkEntriesProductLotItem : draftBulkEntriesProduct
          .getDraftLotItemListWrapper()) {
        productLotItemGenericDao.createOrUpdate(draftBulkEntriesProductLotItem);
      }
      return null;
    });
  }

  public List<DraftBulkEntriesProduct> queryAllBulkEntriesDraft() throws LMISException {
    return productGenericDao.queryForAll();
  }

  public void clearBulkEntriesDraft() throws LMISException {
    dbUtil.withDaoAsBatch(DraftBulkEntriesProduct.class, dao -> {
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftBulkEntriesProduct.class);
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftBulkEntriesProductLotItem.class);
      return null;
    });

  }

}
