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
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.DraftInitialInventoryLotItem;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class InventoryRepository {

  final Context context;
  GenericDao<Inventory> genericDao;
  GenericDao<DraftInventory> draftInventoryGenericDao;
  GenericDao<DraftLotItem> draftLotItemGenericDao;

  GenericDao<DraftInitialInventory> draftInitialInventoryGenericDao;
  GenericDao<DraftInitialInventoryLotItem> draftInitialInventoryLotItemGenericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  public InventoryRepository(Context context) {
    genericDao = new GenericDao<>(Inventory.class, context);
    draftInventoryGenericDao = new GenericDao<>(DraftInventory.class, context);
    draftLotItemGenericDao = new GenericDao<>(DraftLotItem.class, context);
    draftInitialInventoryGenericDao = new GenericDao<>(DraftInitialInventory.class, context);
    draftInitialInventoryLotItemGenericDao = new GenericDao<>(DraftInitialInventoryLotItem.class,
        context);
    this.context = context;
  }

  public void save(Inventory inventory) {
    try {
      genericDao.create(inventory);
    } catch (LMISException e) {
      new LMISException(e, "InventoryRepository.save").reportToFabric();
    }
  }

  public List<Inventory> queryPeriodInventory(final Period period) throws LMISException {
    return dbUtil.withDao(Inventory.class, dao -> dao.queryBuilder().orderBy("updatedAt", false)
        .where()
        .between("updatedAt",
            period.getInventoryBegin().toDate(),
            period.getInventoryEnd().toDate())
        .query());
  }

  public void createDraft(final DraftInventory draftInventory) throws LMISException {
    dbUtil.withDaoAsBatch(DraftInventory.class, dao -> {
      draftInventoryGenericDao.createOrUpdate(draftInventory);
      for (DraftLotItem draftLotItem : draftInventory.getDraftLotItemListWrapper()) {
        draftLotItemGenericDao.createOrUpdate(draftLotItem);
      }
      return null;
    });
  }

  public List<DraftInventory> queryAllDraft() throws LMISException {
    return draftInventoryGenericDao.queryForAll();
  }

  public void clearDraft() throws LMISException {
    dbUtil.withDaoAsBatch(DraftInventory.class, dao -> {
      TableUtils
          .clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(),
              DraftInventory.class);
      TableUtils
          .clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(),
              DraftLotItem.class);
      return null;
    });

  }

  public void createInitialDraft(final DraftInitialInventory draftInitialInventory)
      throws LMISException {
    dbUtil.withDaoAsBatch(DraftInitialInventory.class, dao -> {
      draftInitialInventoryGenericDao.createOrUpdate(draftInitialInventory);
      for (DraftInitialInventoryLotItem draftInitialInventoryLotItem : draftInitialInventory
          .getDraftLotItemListWrapper()) {
        draftInitialInventoryLotItemGenericDao.createOrUpdate(draftInitialInventoryLotItem);
      }
      return null;
    });
  }

  public List<DraftInitialInventory> queryAllInitialDraft() throws LMISException {
    return draftInitialInventoryGenericDao.queryForAll();
  }

  public void clearInitialDraft() throws LMISException {
    dbUtil.withDaoAsBatch(DraftInitialInventory.class, dao -> {
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftInitialInventory.class);
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftInitialInventoryLotItem.class);
      return null;
    });

  }
}
