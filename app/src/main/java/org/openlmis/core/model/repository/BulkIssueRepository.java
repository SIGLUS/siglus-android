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
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.table.TableUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkIssueLot;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class BulkIssueRepository {

  private final GenericDao<DraftBulkIssueProduct> draftProductGenericDao;
  private final GenericDao<DraftBulkIssueLot> draftLotGenericDao;

  @Inject
  private final DbUtil dbUtil;

  @Inject
  public BulkIssueRepository(Context context, DbUtil dbUtil) {
    this.draftProductGenericDao = new GenericDao<>(DraftBulkIssueProduct.class, context);
    this.draftLotGenericDao = new GenericDao<>(DraftBulkIssueLot.class, context);
    this.dbUtil = dbUtil;
  }

  public boolean hasDraft() throws LMISException {
    return CollectionUtils.isNotEmpty(queryUsableBulkIssueDraft());
  }

  public List<DraftBulkIssueProduct> queryUsableBulkIssueDraft() throws LMISException {
    return dbUtil.withDaoAsBatch(DraftBulkIssueProduct.class, dao -> {
      String rawSql = "SELECT * FROM draft_bulk_issue_products "
          + "WHERE product_id IN (SELECT product_id FROM stock_cards WHERE stockOnHand > 0) "
          + "AND product_id IN (SELECT id from products WHERE isKit = 0 "
          + "AND isArchived = 0); ";
      GenericRawResults<DraftBulkIssueProduct> queryResult = dao.queryRaw(rawSql, dao.getRawRowMapper());
      List<DraftBulkIssueProduct> draftProducts = queryResult.getResults();
      Iterator<DraftBulkIssueProduct> productIterator = draftProducts.iterator();
      while (productIterator.hasNext()) {
        DraftBulkIssueProduct draftProduct = productIterator.next();
        dao.assignEmptyForeignCollection(draftProduct, "foreignDraftLots");
        ForeignCollection<DraftBulkIssueLot> foreignDraftLots = draftProduct.getForeignDraftLots();
        Iterator<DraftBulkIssueLot> lotIterator = foreignDraftLots.iterator();
        while (lotIterator.hasNext()) {
          LotOnHand lotOnHand = lotIterator.next().getLotOnHand();
          if (lotOnHand.getQuantityOnHand() == null || lotOnHand.getQuantityOnHand() <= 0) {
            lotIterator.remove();
          }
        }
        if (foreignDraftLots.isEmpty()) {
          productIterator.remove();
        }
      }
      Collections.sort(draftProducts);
      return draftProducts;
    });
  }

  public void deleteDraft() throws LMISException {
    dbUtil.withDaoAsBatch(DraftBulkIssueProduct.class, dao -> {
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftBulkIssueProduct.class);
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftBulkIssueLot.class);
      return null;
    });
  }

  public void saveDraft(List<DraftBulkIssueProduct> draftProducts) throws LMISException {
    if (CollectionUtils.isEmpty(draftProducts)) {
      return;
    }
    dbUtil.withDaoAsBatch(DraftBulkIssueProduct.class, dao -> {
      deleteDraft();
      for (DraftBulkIssueProduct draftProduct : draftProducts) {
        draftProductGenericDao.createOrUpdate(draftProduct);
        for (DraftBulkIssueLot lotItem : draftProduct.getDraftLotListWrapper()) {
          draftLotGenericDao.createOrUpdate(lotItem);
        }
      }
      return null;
    });
  }
}
