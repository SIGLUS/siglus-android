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

package org.openlmis.core.persistence.migrations;


import java.util.List;
import java.util.Locale;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.Migration;


public class ChangeMovementReasonToCode extends Migration {

  GenericDao<StockMovementItem> stockItemGenericDao;


  MovementReasonManager reasonManager;
  DbUtil dbUtil;

  public ChangeMovementReasonToCode() {
    stockItemGenericDao = new GenericDao<>(StockMovementItem.class, LMISApp.getContext());
    reasonManager = MovementReasonManager.getInstance();
    dbUtil = new DbUtil();
  }

  @Override
  public void up() {
    try {
      List<StockMovementItem> itemList = stockItemGenericDao.queryForAll();
      if (itemList == null || itemList.size() == 0) {
        return;
      }
      for (StockMovementItem item : itemList) {
        boolean isReasonSet = trySetReason(item, "pt", "pt") || trySetReason(item, "en", "us");
        if (!isReasonSet) {
          setDefaultReasonCode(item);
        }
      }
      updateStockMovementItems(itemList);

    } catch (LMISException e) {
      new LMISException(e, "ChangeMovementReasonToCode,up").reportToFabric();
      throw new RuntimeException(e.getMessage());
    }
  }

  protected void setDefaultReasonCode(StockMovementItem item) {
    if ("physicalInventoryPositive".equalsIgnoreCase(item.getReason())) {
      item.setReason(MovementReasonManager.INVENTORY_POSITIVE);
    } else if ("physicalInventoryNegative".equalsIgnoreCase(item.getReason())) {
      item.setReason(MovementReasonManager.INVENTORY_NEGATIVE);
    } else {
      switch (item.getMovementType()) {
        case ISSUE:
          item.setReason(MovementReasonManager.DEFAULT_ISSUE);
          break;
        case POSITIVE_ADJUST:
          item.setReason(MovementReasonManager.DEFAULT_POSITIVE_ADJUSTMENT);
          break;
        case NEGATIVE_ADJUST:
          item.setReason(MovementReasonManager.DEFAULT_NEGATIVE_ADJUSTMENT);
          break;
        case RECEIVE:
          item.setReason(MovementReasonManager.DEFAULT_RECEIVE);
          break;
        case PHYSICAL_INVENTORY:
          item.setReason(MovementReasonManager.INVENTORY);
          break;
        default:
          throw new RuntimeException("Invalid MovementType :" + item.getMovementType());
      }
    }
  }

  private void updateStockMovementItems(final List<StockMovementItem> stockMovementItems)
      throws LMISException {
    dbUtil.withDaoAsBatch(LMISApp.getContext(), StockMovementItem.class,
        (DbUtil.Operation<StockMovementItem, Void>) dao -> {
          for (StockMovementItem stockMovementItem : stockMovementItems) {
            dao.update(stockMovementItem);
          }
          return null;
        });
  }

  private boolean trySetReason(StockMovementItem item, String lang, String country) {
    try {
      MovementReason reason = reasonManager
          .queryByDesc(item.getReason(), new Locale(lang, country));
      item.setReason(reason.getCode());
      return true;
    } catch (MovementReasonNotFoundException ignored) {
      return false;
    }
  }
}
