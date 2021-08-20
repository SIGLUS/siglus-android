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

package org.openlmis.core.view.viewmodel;

import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.helper.MovementQuantityHelper;
import org.openlmis.core.utils.DateUtil;

@Data
public class StockHistoryMovementItemViewModel {

  StockMovementItem stockMovementItem;
  private String movementDate;
  private static String EMPTY_FIELD = "-";

  private Map<MovementType, String> typeQuantityMap;

  public StockHistoryMovementItemViewModel(StockMovementItem item) {
    this.stockMovementItem = item;
    typeQuantityMap = MovementQuantityHelper
        .generateTypeQuantityMap(item.getMovementType(), item.getReason(), item.getMovementQuantity());
  }

  public String getMovementDate() {
    return DateUtil.formatDate(stockMovementItem.getMovementDate());
  }

  public String getMovementReason() {
    try {
      return MovementReasonManager.getInstance()
          .queryByCode(stockMovementItem.getMovementType(), stockMovementItem.getReason())
          .getDescription();
    } catch (MovementReasonNotFoundException e) {
      return "";
    }
  }

  public String getDocumentNumber() {
    if (StringUtils.isEmpty(stockMovementItem.getDocumentNumber())) {
      return EMPTY_FIELD;
    }
    return stockMovementItem.getDocumentNumber();
  }

  public String getEntryAmount() {
    return wrapOrEmpty(typeQuantityMap.get(MovementReasonManager.MovementType.RECEIVE));
  }

  public String getNegativeAdjustmentAmount() {
    return wrapOrEmpty(typeQuantityMap.get(MovementReasonManager.MovementType.NEGATIVE_ADJUST));
  }

  public String getPositiveAdjustmentAmount() {
    return wrapOrEmpty(typeQuantityMap.get(MovementReasonManager.MovementType.POSITIVE_ADJUST));
  }

  public String getIssueAmount() {
    return wrapOrEmpty(typeQuantityMap.get(MovementReasonManager.MovementType.ISSUE));
  }

  public String getStockExistence() {
    return String.valueOf(stockMovementItem.getStockOnHand());
  }

  private String wrapOrEmpty(String s) {
    if (StringUtils.isEmpty(s)) {
      return EMPTY_FIELD;
    } else {
      return s;
    }
  }

  public boolean isIssueAdjustment() {
    return stockMovementItem.getMovementType().equals(MovementReasonManager.MovementType.ISSUE);
  }
}
