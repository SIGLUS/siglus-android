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
import lombok.Setter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.PodLotItem;
import org.openlmis.core.model.PodProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

public class PodLotItemRepository {

  private final GenericDao<PodLotItem> podLotItemGenericDao;

  @Inject
  private final DbUtil dbUtil;

  private final Context context;

  @Inject
  LotRepository lotRepository;

  @Setter
  @Inject
  ProductRepository productRepository;

  @Inject
  public PodLotItemRepository(Context context, DbUtil dbUtil) {
    this.podLotItemGenericDao = new GenericDao<>(PodLotItem.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public PodLotItem queryByPodProductIdAndLotId(long podProductId, long lotId) throws LMISException {
    return dbUtil.withDao(PodLotItem.class,
        dao -> dao.queryBuilder()
            .where()
            .eq(FieldConstants.POD_PRODUCT_ID, podProductId)
            .and()
            .eq(FieldConstants.LOT_ID, lotId)
            .queryForFirst());
  }

  public void batchCreatePodLotItemsWithLotInfo(@Nullable final List<PodLotItem> podLotItems, PodProduct podProduct)
      throws LMISException {
    if (podLotItems == null) {
      return;
    }
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (PodLotItem podLotItem : podLotItems) {
          Product product = productRepository.getByCode(podProduct.getCode());
          Lot lot = podLotItem.getLot();
          lot.setProduct(product);
          Lot savedLot = lotRepository.createOrUpdate(lot);
          podLotItem.setPodProduct(podProduct);
          podLotItem.setLot(savedLot);
          createOrUpdateItem(podLotItem);
        }
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  private void createOrUpdateItem(final PodLotItem podLotItem) throws LMISException {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              () -> {
                podLotItem.setCreatedAt(DateUtil.getCurrentDate());
                podLotItem.setUpdatedAt(DateUtil.getCurrentDate());
                createdOrUpdate(podLotItem);
                return null;
              });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  private void createdOrUpdate(PodLotItem podLotItem) throws LMISException {
    PodLotItem existingPodLotItem = queryByPodProductIdAndLotId(podLotItem.getPodProduct().getId(),
        podLotItem.getLot().getId());
    if (existingPodLotItem != null) {
      podLotItem.setId(existingPodLotItem.getId());
    }
    podLotItemGenericDao.createOrUpdate(podLotItem);
  }
}
