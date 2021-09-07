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

import static org.openlmis.core.constant.FieldConstants.ORDER_STATUS;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.network.SyncErrorsMap;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

public class PodRepository {

  private final GenericDao<Pod> podGenericDao;

  @Inject
  private final DbUtil dbUtil;

  private final Context context;

  @Setter
  @Inject
  PodProductItemRepository podProductItemRepository;

  @Inject
  SyncErrorsRepository syncErrorsRepository;

  @Inject
  public PodRepository(Context context, DbUtil dbUtil) {
    this.podGenericDao = new GenericDao<>(Pod.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public List<Pod> list() throws LMISException {
    return dbUtil.withDao(Pod.class, dao -> dao.queryBuilder().query());
  }

  public Pod queryById(long id) throws LMISException {
    return podGenericDao.getById(String.valueOf(id));
  }

  public List<Pod> queryByStatus(OrderStatus status) throws LMISException {
    return dbUtil.withDao(Pod.class, dao -> dao.queryBuilder().where().eq(ORDER_STATUS, status).query());
  }

  public Pod queryByOrderCode(String orderCode) throws LMISException {
    return dbUtil.withDao(Pod.class,
        dao -> dao.queryBuilder().where().eq(FieldConstants.ORDER_CODE, orderCode).queryForFirst());
  }

  public List<String> querySameProgramIssueVoucherByOrderCode(String orderCode) {
    String rawSql = "SELECT orderCode FROM pods WHERE requisitionProgramCode"
        + " IN (SELECT requisitionProgramCode FROM pods WHERE orderCode = '" + orderCode + "')"
        + " AND orderStatus = 'SHIPPED'"
        + " AND isLocal = 0";
    Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
    List<String> matchedOrderCodes = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        matchedOrderCodes.add(cursor.getString(cursor.getColumnIndexOrThrow(FieldConstants.ORDER_CODE)));
      } while (cursor.moveToNext());
    }
    if (!cursor.isClosed()) {
      cursor.close();
    }
    return matchedOrderCodes;
  }

  public boolean hasUnmatchedPodByProgram(String programCode) {
    String rawSql = "SELECT errorMessage FROM sync_errors WHERE syncType = 'POD'"
        + " AND syncObjectId IN (SELECT id FROM pods WHERE requisitionProgramCode = '" + programCode + "'"
        + " AND isSynced = 0)";
    try (Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase()
        .rawQuery(rawSql, null)) {
      if (!cursor.moveToFirst()) {
        return false;
      }
      do {
        String errorMsg = cursor.getString(cursor.getColumnIndexOrThrow(FieldConstants.ERROR_MESSAGE));
        if (StringUtils.isNotEmpty(errorMsg) && errorMsg.contains(SyncErrorsMap.ERROR_POD_ORDER_DOSE_NOT_EXIST)) {
          return true;
        }
      } while (cursor.moveToNext());
      return false;
    }
  }

  public void updateOrderCode(String podOrderCode, String issueVoucherOrderCode) throws LMISException {
    dbUtil.withDaoAsBatch(Pod.class, dao -> {
      // delete chosen issueVoucher
      Pod issueVoucher = dao.queryBuilder()
          .where().eq(FieldConstants.ORDER_CODE, issueVoucherOrderCode)
          .queryForFirst();
      for (PodProductItem podProductItem : issueVoucher.getPodProductItemsWrapper()) {
        podProductItemRepository.delete(podProductItem);
      }
      dao.delete(issueVoucher);

      // set origin order code and update pod
      Pod pod = dao.queryBuilder().where().eq(FieldConstants.ORDER_CODE, podOrderCode).queryForFirst();
      if (StringUtils.isEmpty(pod.getOriginOrderCode())) {
        pod.setOriginOrderCode(podOrderCode);
      }
      pod.setOrderCode(issueVoucherOrderCode);
      dao.update(pod);

      // delete sync error
      syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.POD, pod.getId());
      return null;
    });
  }

  public void deleteByOrderCode(String orderCode) throws LMISException {
    dbUtil.withDaoAsBatch(Pod.class, dao -> {
      Pod pod = dao.queryBuilder().where().eq(FieldConstants.ORDER_CODE, orderCode).queryForFirst();
      if (pod == null) {
        return null;
      }
      for (PodProductItem podProductItem : pod.getPodProductItemsWrapper()) {
        podProductItemRepository.delete(podProductItem);
      }
      dao.delete(pod);
      return null;
    });
  }

  public boolean hasOldData() {
    try {
      List<Pod> list = list();
      int definedOffsetMonth = SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData();
      Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(DateUtil.getCurrentDate(), definedOffsetMonth);
      for (Pod pod : list) {
        Date requisitionEndDate = pod.getRequisitionEndDate();
        if (requisitionEndDate != null && requisitionEndDate.before(dueDateShouldDataLivedInDB)) {
          return true;
        }
      }
    } catch (LMISException e) {
      new LMISException(e, "PodRepository.hasOldDate").reportToFabric();
    }
    return false;
  }

  public void deleteOldData() {
    String dueDateShouldDataLivedInDB = DateUtil.formatDate(DateUtil.dateMinusMonth(DateUtil.getCurrentDate(),
        SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData()), DateUtil.DB_DATE_FORMAT);

    String rawSqlDeletePod = "DELETE FROM pods"
        + " WHERE requisitionEndDate NOT NULL AND requisitionEndDate < '" + dueDateShouldDataLivedInDB + "'";

    String rawSqlDeletePodProductItems = "DELETE FROM pod_product_items"
        + " WHERE pod_id IN (SELECT id FROM pods WHERE requisitionEndDate NOT NULL AND requisitionEndDate < '"
        + dueDateShouldDataLivedInDB + "')";

    String rawSqlDeletePodLotItems = "DELETE FROM pod_product_lot_items"
        + " WHERE podProductItem_id IN (SELECT id FROM pod_product_items"
        + " WHERE pod_id IN (SELECT id FROM pods WHERE requisitionEndDate NOT NULL AND requisitionEndDate < '"
        + dueDateShouldDataLivedInDB + "'))";

    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeletePodLotItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeletePodProductItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeletePod);
  }

  public void batchCreatePodsWithItems(@Nullable final List<Pod> pods) throws LMISException {
    if (pods == null) {
      return;
    }
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (Pod pod : pods) {
          pod.setCreatedAt(DateUtil.getCurrentDate());
          pod.setUpdatedAt(DateUtil.getCurrentDate());
          createOrUpdateWithItems(pod);
        }
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  public void createOrUpdateWithItems(final Pod pod) throws LMISException {
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
          () -> {
            Pod savedPod = createOrUpdate(pod);
            podProductItemRepository.batchCreatePodProductsWithItems(pod.getPodProductItemsWrapper(), savedPod);
            return null;
          });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  private Pod createOrUpdate(Pod pod) throws LMISException {
    Pod existingPod = queryByOrderCode(pod.getOrderCode());
    if (existingPod != null) {
      pod.setId(existingPod.getId());
    }
    return podGenericDao.createOrUpdate(pod);
  }
}
