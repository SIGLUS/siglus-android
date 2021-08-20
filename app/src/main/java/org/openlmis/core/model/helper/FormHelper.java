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

package org.openlmis.core.model.helper;

import java.util.List;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.StockMovementItem;

public class FormHelper {

  public class StockMovementModifiedItem {

    long totalReceived = 0;
    long totalIssued = 0;
    long totalAdjustment = 0;

    public long getTotalReceived() {
      return totalReceived;
    }

    public long getTotalIssued() {
      return totalIssued;
    }

    public long getTotalAdjustment() {
      return totalAdjustment;
    }
  }

  public StockMovementModifiedItem assignTotalValues(List<StockMovementItem> stockMovementItems) {
    StockMovementModifiedItem modifiedItem = new StockMovementModifiedItem();

    for (StockMovementItem item : stockMovementItems) {
      MovementType movementType = item.getMovementType();
      String reason = item.getReason();
      if (MovementType.RECEIVE == movementType) {
        modifiedItem.totalReceived += item.getMovementQuantity();
      } else if (MovementType.ISSUE == movementType) {
        modifiedItem.totalIssued += item.getMovementQuantity();
      } else if (MovementType.NEGATIVE_ADJUST == movementType
          || MovementReasonManager.INVENTORY_NEGATIVE.equalsIgnoreCase(reason)) {
        modifiedItem.totalAdjustment -= item.getMovementQuantity();
      } else if (MovementType.POSITIVE_ADJUST == movementType
          || MovementReasonManager.INVENTORY_POSITIVE.equalsIgnoreCase(reason)) {
        modifiedItem.totalAdjustment += item.getMovementQuantity();
      }
    }
    return modifiedItem;
  }
}
