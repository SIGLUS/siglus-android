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
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

public class PodProductLotItemRepository {

  private final GenericDao<PodProductLotItem> podProductLotItemGenericDao;

  @Inject
  private final DbUtil dbUtil;

  private final Context context;

  @Inject
  LotRepository lotRepository;

  @Setter
  @Inject
  ProductRepository productRepository;

  @Inject
  public PodProductLotItemRepository(Context context, DbUtil dbUtil) {
    this.podProductLotItemGenericDao = new GenericDao<>(PodProductLotItem.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public PodProductLotItem queryByPodProductIdAndLotId(long podProductItemId, long lotId) throws LMISException {
    return dbUtil.withDao(PodProductLotItem.class,
        dao -> dao.queryBuilder()
            .where()
            .eq(FieldConstants.POD_PRODUCT_ITEM_ID, podProductItemId)
            .and()
            .eq(FieldConstants.LOT_ID, lotId)
            .queryForFirst());
  }

  public void delete(PodProductLotItem lotItem) throws LMISException {
    podProductLotItemGenericDao.delete(lotItem);
  }

  public void batchCreatePodLotItemsWithLotInfo(@Nullable final List<PodProductLotItem> podProductLotItems,
      PodProductItem podProductItem)
      throws LMISException {
    if (podProductLotItems == null) {
      return;
    }
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (PodProductLotItem podProductLotItem : podProductLotItems) {
          Lot lot = podProductLotItem.getLot();
          if (lot != null && !Constants.VIRTUAL_LOT_NUMBER.equals(lot.getLotNumber())) {
            lot.setProduct(podProductItem.getProduct());
            Lot savedLot = lotRepository.createOrUpdateWithExistingLot(lot);
            podProductLotItem.setLot(savedLot);
          }
          podProductLotItem.setPodProductItem(podProductItem);
          createOrUpdateItem(podProductLotItem);
        }
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  private void createOrUpdateItem(final PodProductLotItem podProductLotItem) throws LMISException {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              () -> {
                podProductLotItem.setCreatedAt(DateUtil.getCurrentDate());
                podProductLotItem.setUpdatedAt(DateUtil.getCurrentDate());
                createdOrUpdate(podProductLotItem);
                return null;
              });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  private void createdOrUpdate(PodProductLotItem podProductLotItem) throws LMISException {
    if (podProductLotItem.getLot() != null) {
      PodProductLotItem existingPodProductLotItem = queryByPodProductIdAndLotId(
          podProductLotItem.getPodProductItem().getId(),
          podProductLotItem.getLot().getId());
      if (existingPodProductLotItem != null) {
        podProductLotItem.setId(existingPodProductLotItem.getId());
      }
    }
    podProductLotItemGenericDao.createOrUpdate(podProductLotItem);
  }
}
