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
import com.j256.ormlite.stmt.DeleteBuilder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class DirtyDataRepository {

  private static final String TAG = DirtyDataRepository.class.getSimpleName();

  GenericDao<DirtyDataItemInfo> deleteItemInfoGenericDao;

  @Inject
  DbUtil dbUtil;
  @Inject
  StockRepository stockRepository;
  @Inject
  RnrFormRepository rnrFormRepository;
  @Inject
  ProgramRepository programRepository;

  @Inject
  public DirtyDataRepository(Context context) {
    deleteItemInfoGenericDao = new GenericDao<>(DirtyDataItemInfo.class, context);
  }

  public void saveAll(List<DirtyDataItemInfo> dirtyDataItemsInfo) {
    try {
      List<DirtyDataItemInfo> itemInfos = listAll();
      Map<String, DirtyDataItemInfo> productCodeMapItem = new HashMap<>();
      for (DirtyDataItemInfo info : itemInfos) {
        productCodeMapItem.put(info.getProductCode(), info);
      }
      dbUtil.withDaoAsBatch(DirtyDataItemInfo.class, dao -> {
        for (DirtyDataItemInfo item : dirtyDataItemsInfo) {
          if (productCodeMapItem.containsKey(item.getProductCode())) {
            DirtyDataItemInfo dbItem = productCodeMapItem.get(item.getProductCode());
            dbItem.setFullyDelete(item.isFullyDelete());
            dbItem.setSynced(false);
            dbItem.setJsonData(item.getJsonData());
            dao.createOrUpdate(dbItem);
          } else {
            dao.createOrUpdate(item);
          }
        }
        return null;
      });

    } catch (LMISException e) {
      Log.w(TAG, e);
    }
  }

  public void save(DirtyDataItemInfo item) {
    try {
      List<DirtyDataItemInfo> itemInfos = listAll();
      Map<String, DirtyDataItemInfo> productCodeMapItem = new HashMap<>();
      for (DirtyDataItemInfo info : itemInfos) {
        productCodeMapItem.put(info.getProductCode(), info);
      }
      if (productCodeMapItem.containsKey(item.getProductCode())) {
        DirtyDataItemInfo dbItem = productCodeMapItem.get(item.getProductCode());
        dbItem.setSynced(false);
        dbItem.setJsonData(item.getJsonData());
        deleteItemInfoGenericDao.createOrUpdate(dbItem);
      } else {
        deleteItemInfoGenericDao.createOrUpdate(item);
      }

    } catch (LMISException e) {
      Log.w(TAG, e);
    }
  }

  private List<DirtyDataItemInfo> listAll() {
    try {
      return deleteItemInfoGenericDao.queryForAll();
    } catch (LMISException e) {
      Log.w(TAG, e);
    }
    return Collections.emptyList();
  }

  public List<DirtyDataItemInfo> listunSyced() {
    try {
      return FluentIterable.from(deleteItemInfoGenericDao.queryForAll())
          .filter(dirtyDataItemInfo -> !dirtyDataItemInfo.isSynced()).toList();
    } catch (LMISException e) {
      Log.w(TAG, e);
    }
    return Collections.emptyList();
  }

  private boolean hasBackedData(List<DirtyDataItemInfo> infos) {
    return CollectionUtils.isNotEmpty(infos);
  }

  public boolean hasOldDate() {
    Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(DateUtil.getCurrentDate(), 1);
    List<DirtyDataItemInfo> infos = listAll();
    if (hasBackedData(infos)) {
      for (DirtyDataItemInfo itemInfo : infos) {
        if (itemInfo.getCreatedAt().before(dueDateShouldDataLivedInDB)) {
          return true;
        }
      }
    }
    return false;
  }

  public void deleteOldData() {
    Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(DateUtil.getCurrentDate(), 1);
    try {
      dbUtil.withDao(DirtyDataItemInfo.class, dao -> {
        DeleteBuilder<DirtyDataItemInfo, String> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().le(FieldConstants.CREATED_AT, dueDateShouldDataLivedInDB);
        deleteBuilder.delete();
        return null;
      });
    } catch (LMISException e) {
      Log.w(TAG, e);
    }
  }

  public void deleteDraftForDirtyData() {
    String deleteDraftLotItems = "DELETE FROM draft_lot_items";
    String deleteDraftInventory = "DELETE FROM draft_inventory";
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteDraftLotItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteDraftInventory);
  }
}
