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
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProduct;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

public class PodProductRepository {

  private final GenericDao<PodProduct> podProductGenericDao;

  @Inject
  private final DbUtil dbUtil;

  private final Context context;

  @Inject
  PodLotItemRepository podLotItemRepository;

  @Inject
  public PodProductRepository(DbUtil dbUtil, Context context) {
    this.podProductGenericDao = new GenericDao<>(PodProduct.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public void batchCreatePodProductsWithItems(@Nullable final List<PodProduct> podProducts, Pod pod)
      throws LMISException {
    if (podProducts == null) {
      return;
    }
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (PodProduct podProduct : podProducts) {
          podProduct.setPod(pod);
          podProduct.setCreatedAt(DateUtil.getCurrentDate());
          podProduct.setUpdatedAt(DateUtil.getCurrentDate());
          createOrUpdateWithItems(podProduct);
        }
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  public PodProduct queryByPodIdAndProductCode(long podId, String productCode) throws LMISException {
    return dbUtil.withDao(PodProduct.class,
        dao -> dao.queryBuilder()
            .where().eq(FieldConstants.POD_ID, podId)
            .and().eq(FieldConstants.CODE, productCode)
            .queryForFirst());
  }

  private void createOrUpdateWithItems(final PodProduct podProduct) throws LMISException {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              () -> {
                PodProduct savedPodProduct = createOrUpdate(podProduct);
                podLotItemRepository
                    .batchCreatePodLotItemsWithLotInfo(podProduct.getPodLotItemsWrapper(), savedPodProduct);
                return null;
              });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }


  private PodProduct createOrUpdate(PodProduct podProduct) throws LMISException {
    PodProduct existingPodProduct = queryByPodIdAndProductCode(podProduct.getPod().getId(), podProduct.getCode());
    if (existingPodProduct != null) {
      podProduct.setId(existingPodProduct.getId());
    }
    return podProductGenericDao.createOrUpdate(podProduct);
  }

}
