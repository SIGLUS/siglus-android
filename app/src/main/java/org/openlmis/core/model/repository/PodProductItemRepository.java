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
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import java.sql.SQLException;
import java.util.List;
import lombok.Setter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

public class PodProductItemRepository {

  private final GenericDao<PodProductItem> podProductItemGenericDao;

  @Inject
  private final DbUtil dbUtil;

  private final Context context;

  @Setter
  @Inject
  PodProductLotItemRepository podProductLotItemRepository;

  @Inject
  public PodProductItemRepository(DbUtil dbUtil, Context context) {
    this.podProductItemGenericDao = new GenericDao<>(PodProductItem.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public void batchCreatePodProductsWithItems(@Nullable final List<PodProductItem> podProductItems, Pod pod)
      throws LMISException {
    if (podProductItems == null) {
      return;
    }
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (PodProductItem podProductItem : podProductItems) {
          podProductItem.setPod(pod);
          podProductItem.setCreatedAt(DateUtil.getCurrentDate());
          podProductItem.setUpdatedAt(DateUtil.getCurrentDate());
          createOrUpdateWithItems(podProductItem);
        }
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  public PodProductItem queryByPodIdAndProductId(long podId, long productId) throws LMISException {
    return dbUtil.withDao(PodProductItem.class,
        dao -> dao.queryBuilder()
            .where().eq(FieldConstants.POD_ID, podId)
            .and().eq(FieldConstants.PRODUCT_ID, productId)
            .queryForFirst());
  }

  public void delete(PodProductItem productItem) throws LMISException {
    dbUtil.withDaoAsBatch(PodProductItem.class, dao -> {
      for (PodProductLotItem podProductLotItem : productItem.getPodProductLotItemsWrapper()) {
        podProductLotItemRepository.delete(podProductLotItem);
      }
      podProductItemGenericDao.delete(productItem);
      return null;
    });
  }

  public void deleteByPodId(long podId) {

    String rawSqlDeletePodProductItems = "DELETE FROM pod_product_items"
        + " WHERE pod_id = " + podId;

    String rawSqlDeletePodLotItems = "DELETE FROM pod_product_lot_items"
        + " WHERE podProductItem_id IN (SELECT id FROM pod_product_items"
        + " WHERE pod_id = " + podId + ")";

    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeletePodLotItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeletePodProductItems);
  }

  private void createOrUpdateWithItems(final PodProductItem podProductItem) throws LMISException {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              () -> {
                PodProductItem savedPodProductItem = createOrUpdate(podProductItem);
                podProductLotItemRepository
                    .batchCreatePodLotItemsWithLotInfo(podProductItem.getPodProductLotItemsWrapper(),
                        savedPodProductItem);
                return null;
              });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  private PodProductItem createOrUpdate(PodProductItem podProductItem) throws LMISException {
    PodProductItem existingPodProductItem = queryByPodIdAndProductId(podProductItem.getPod().getId(),
        podProductItem.getProduct().getId());
    if (existingPodProductItem != null) {
      podProductItem.setId(existingPodProductItem.getId());
    }
    return podProductItemGenericDao.createOrUpdate(podProductItem);
  }

}
