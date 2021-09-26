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
import com.j256.ormlite.table.TableUtils;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DraftBulkIssueLot;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class BulkIssueRepository {

  private final GenericDao<DraftBulkIssueProduct> draftProductGenericDao;
  private final GenericDao<DraftBulkIssueLot> draftLotGenericDao;

  @Inject
  private final DbUtil dbUtil;

  @Inject
  private StockRepository stockRepository;

  @Inject
  public BulkIssueRepository(Context context, DbUtil dbUtil) {
    this.draftProductGenericDao = new GenericDao<>(DraftBulkIssueProduct.class, context);
    this.draftLotGenericDao = new GenericDao<>(DraftBulkIssueLot.class, context);
    this.dbUtil = dbUtil;
  }

  public boolean hasDraft() throws LMISException {
    return CollectionUtils.isNotEmpty(queryUsableProductDraft());
  }

  public List<DraftBulkIssueProduct> queryUsableProductAndLotDraft() throws LMISException {
    return dbUtil.withDaoAsBatch(DraftBulkIssueProduct.class, dao -> {
      List<DraftBulkIssueProduct> draftProducts = queryUsableProductDraft();
      for (DraftBulkIssueProduct draftProduct : draftProducts) {
        dao.assignEmptyForeignCollection(draftProduct, FieldConstants.FOREIGN_DRAFT_LOTS);
      }
      Collections.sort(draftProducts);
      return draftProducts;
    });
  }

  private List<DraftBulkIssueProduct> queryUsableProductDraft() throws LMISException {
    return dbUtil.withDaoAsBatch(DraftBulkIssueProduct.class, dao -> {
      String rawSql = "SELECT * FROM draft_bulk_issue_products "
          + "WHERE stockCard_id IN (SELECT id FROM stock_cards WHERE stockOnHand > 0 "
          + "AND product_id IN (SELECT id from products WHERE isKit = 0 AND isArchived = 0)); ";
      return dao.queryRaw(rawSql, dao.getRawRowMapper()).getResults();
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

  public void saveMovement(List<StockMovementItem> stockMovementItems) throws LMISException {
    AtomicBoolean failedWithErrorDate = new AtomicBoolean(false);
    try {
      dbUtil.withDaoAsBatch(DraftBulkIssueProduct.class, dao -> {
        long createdTime = LMISApp.getInstance().getCurrentTimeMillis();
        for (StockMovementItem stockMovementItem : stockMovementItems) {
          StockCard stockCard = stockMovementItem.getStockCard();
          Date lastMovementCreateDate = stockCard.getLastStockMovementCreatedTime();
          if (lastMovementCreateDate != null && stockMovementItem.getCreatedAt().before(lastMovementCreateDate)) {
            failedWithErrorDate.set(true);
            throw new IllegalArgumentException();
          }
          stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem, createdTime);
          if (stockCard.calculateSOHFromLots() == 0 && !stockCard.getProduct().isActive()) {
            SharedPreferenceMgr.getInstance()
                .setIsNeedShowProductsUpdateBanner(true, stockCard.getProduct().getPrimaryName());
          }
        }
        return null;
      });
    } catch (LMISException e) {
      if (failedWithErrorDate.get()) {
        throw new LMISException(e, LMISApp.getContext().getResources().getString(R.string.msg_invalid_stock_movement));
      }
      throw e;
    }
  }
}
