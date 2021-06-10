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

import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockMovementItem;

public class RnrFormHelper {

  @Inject
  FormHelper formHelper;

  public void initRnrFormItemWithoutMovement(RnrFormItem rnrFormItem, long lastRnrInventory)
      throws LMISException {
    rnrFormItem.setReceived(0);
    rnrFormItem.setIssued((long) 0);
    rnrFormItem.setAdjustment((long) 0);
    rnrFormItem.setCalculatedOrderQuantity(0L);
    rnrFormItem.setInitialAmount(lastRnrInventory);
    rnrFormItem.setInventory(lastRnrInventory);
  }

  public void assignTotalValues(RnrFormItem rnrFormItem,
      List<StockMovementItem> stockMovementItems) {
    FormHelper.StockMovementModifiedItem modifiedItem = formHelper
        .assignTotalValues(stockMovementItems);
    rnrFormItem.setReceived(modifiedItem.totalReceived);
    rnrFormItem.setIssued(modifiedItem.totalIssued);
    rnrFormItem.setAdjustment(modifiedItem.totalAdjustment);

    Long inventory = stockMovementItems.get(stockMovementItems.size() - 1).getStockOnHand();
    rnrFormItem.setInventory(inventory);

    rnrFormItem
        .setCalculatedOrderQuantity(calculatedOrderQuantity(modifiedItem.totalIssued, inventory));
  }

  private long calculatedOrderQuantity(long totalIssued, Long inventory) {
    Long totalRequest = totalIssued * 2 - inventory;
    return totalRequest > 0 ? totalRequest : 0;
  }

}
