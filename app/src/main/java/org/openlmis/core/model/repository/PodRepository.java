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
import static org.openlmis.core.constant.FieldConstants.ID;
import android.content.Context;
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import java.sql.SQLException;
import java.util.List;
import lombok.Setter;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
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
  public PodRepository(Context context, DbUtil dbUtil) {
    this.podGenericDao = new GenericDao<>(Pod.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public Pod queryPod(final long id) throws LMISException {
    Pod pod = dbUtil
        .withDao(Pod.class, dao -> dao.queryBuilder().where().eq(ID, id).queryForFirst());

    return pod;
  }

  public List<Pod> listAllPods() throws LMISException {
    return dbUtil.withDao(Pod.class, dao -> dao.queryBuilder().query());
  }

  public List<Pod> queryPodsByStatus(OrderStatus status) throws LMISException {
    return dbUtil.withDao(Pod.class, dao -> dao.queryBuilder().where().eq(ORDER_STATUS, status).query());
  }

  public Pod queryByOrderCode(String orderCode) throws LMISException {
    return dbUtil.withDao(Pod.class,
        dao -> dao.queryBuilder().where().eq(FieldConstants.ORDER_CODE, orderCode).queryForFirst());
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

  public long queryLocalDraftCount() {
    try {
      return dbUtil.withDao(Pod.class,
          dao -> dao.queryBuilder()
              .where().eq(FieldConstants.IS_LOCAL, true)
              .and().eq(FieldConstants.IS_DRAFT, true)
              .countOf());
    } catch (LMISException e) {
      return -1;
    }
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

  private void createOrUpdateWithItems(final Pod pod) throws LMISException {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
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
