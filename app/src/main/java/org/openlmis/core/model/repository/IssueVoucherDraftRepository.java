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

import static org.openlmis.core.constant.FieldConstants.POD_ID;

import android.content.Context;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftIssueVoucherProductItem;
import org.openlmis.core.model.DraftIssueVoucherProductLotItem;
import org.openlmis.core.model.Pod;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class IssueVoucherDraftRepository {

  private final GenericDao<DraftIssueVoucherProductItem> productItemGenericDao;

  private final GenericDao<DraftIssueVoucherProductLotItem> productLotItemGenericDao;

  @Inject
  private final DbUtil dbUtil;

  @Inject
  private PodRepository podRepository;

  @Inject
  public IssueVoucherDraftRepository(Context context, DbUtil dbUtil) {
    this.productItemGenericDao = new GenericDao<>(DraftIssueVoucherProductItem.class, context);
    this.productLotItemGenericDao = new GenericDao<>(DraftIssueVoucherProductLotItem.class, context);
    this.dbUtil = dbUtil;
  }

  public void saveDraft(final List<DraftIssueVoucherProductItem> productItems, Pod pod) throws LMISException {
    if (productItems.isEmpty()) {
      return;
    }
    dbUtil.withDaoAsBatch(DraftIssueVoucherProductItem.class, dao -> {
      deleteIssueVoucherDraftProductAndLot(pod.getId());
      if (pod.getId() == 0) {
        podRepository.batchCreatePodsWithItems(Collections.singletonList(pod));
      }
      for (DraftIssueVoucherProductItem productItem : productItems) {
        productItemGenericDao.createOrUpdate(productItem);
        for (DraftIssueVoucherProductLotItem productLotItem : productItem.getDraftLotItemListWrapper()) {
          productLotItemGenericDao.createOrUpdate(productLotItem);
        }
      }
      return null;
    });
  }

  public List<DraftIssueVoucherProductItem> listAll() throws LMISException {
    return productItemGenericDao.queryForAll();
  }

  public List<DraftIssueVoucherProductItem> queryByPodId(long podId) throws LMISException {
    return dbUtil.withDao(DraftIssueVoucherProductItem.class, dao ->
        dao.queryBuilder().where().eq("pod_id", podId).query());
  }

  public boolean hasDraft(long podId) throws LMISException {
    DraftIssueVoucherProductItem productItem = dbUtil.withDao(DraftIssueVoucherProductItem.class, dao ->
        dao.queryBuilder()
            .where()
            .eq("pod_id", podId)
            .queryForFirst());
    return productItem != null;
  }

  private void deleteIssueVoucherDraftProductAndLot(long podId) {
    String rawSqlDeleteDraftProductItems = "DELETE FROM draft_issue_voucher_product_items"
        + " WHERE " + POD_ID + " = " + podId;

    String rawSqlDeleteProductLotItems = "DELETE FROM draft_issue_voucher_product_lot_items"
        + " WHERE draftIssueVoucherProductItem_id IN (SELECT id FROM draft_issue_voucher_product_items WHERE pod_id = "
        + podId + ")";

    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteProductLotItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteDraftProductItems);
  }
}
