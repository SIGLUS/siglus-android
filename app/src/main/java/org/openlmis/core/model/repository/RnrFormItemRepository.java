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

import static org.openlmis.core.constant.FieldConstants.ID;
import static org.openlmis.core.constant.FieldConstants.INVENTORY;
import static org.openlmis.core.constant.FieldConstants.IS_MANUAL_ADD;
import static org.openlmis.core.constant.FieldConstants.PRODUCT_ID;

import android.content.Context;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class RnrFormItemRepository {

  GenericDao<RnrFormItem> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  private ServiceItemRepository serviceItemRepository;


  @Inject
  public RnrFormItemRepository(Context context) {
    this.genericDao = new GenericDao<>(RnrFormItem.class, context);
  }

  public List<RnrFormItem> queryListForLowStockByProductId(final Product product)
      throws LMISException {
    return dbUtil.withDao(RnrFormItem.class,
        dao -> dao.queryBuilder()
            .orderBy(ID, false)
            .limit(3L)
            .where().eq(PRODUCT_ID, product.getId())
            .and().ne(INVENTORY, 0)
            .query());
  }

  public void batchCreateOrUpdate(final List<RnrFormItem> rnrFormItemList) throws LMISException {
    batchCreateOrUpdateItems(rnrFormItemList);
    for (RnrFormItem item : rnrFormItemList) {
      List<ServiceItem> serviceItems = item.getServiceItemListWrapper();
      if (!serviceItems.isEmpty()) {
        serviceItemRepository.batchCreateOrUpdate(serviceItems);
      }
    }
  }

  private void batchCreateOrUpdateItems(final List<RnrFormItem> rnrFormItemList) throws LMISException {
    dbUtil.withDaoAsBatch(RnrFormItem.class, dao -> {
      for (RnrFormItem item : rnrFormItemList) {
        dao.createOrUpdate(item);
      }
      return null;
    });
  }

  public void deleteFormItems(final List<RnrFormItem> rnrFormItemListWrapper) throws LMISException {
    batchDeleteItems(rnrFormItemListWrapper);
    for (RnrFormItem item : rnrFormItemListWrapper) {
      List<ServiceItem> serviceItems = item.getServiceItemListWrapper();
      if (!serviceItems.isEmpty()) {
        serviceItemRepository.deleteServiceItems(serviceItems);
      }
    }
  }

  private void batchDeleteItems(final List<RnrFormItem> rnrFormItemListWrapper) throws LMISException {
    dbUtil.withDaoAsBatch(RnrFormItem.class, dao -> {
      for (RnrFormItem item : rnrFormItemListWrapper) {
        dao.delete(item);
      }
      return null;
    });
  }

  public List<RnrFormItem> listAllNewRnrItems() throws LMISException {
    return dbUtil.withDao(RnrFormItem.class, dao -> dao.queryBuilder().where().eq(IS_MANUAL_ADD, true).query());
  }

  public void deleteRnrItem(final RnrFormItem rnrFormItem) throws LMISException {
    dbUtil.withDaoAsBatch(RnrFormItem.class, dao -> {
      dao.delete(rnrFormItem);
      return null;
    });
  }
}
