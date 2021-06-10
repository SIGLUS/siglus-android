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

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.Product;

@Data
public class UnpackKitInventoryViewModel extends InventoryViewModel {

  private boolean shouldShowEmptyLotWarning = false;
  private boolean confirmedNoStockReceived = false;

  public UnpackKitInventoryViewModel(Product product) {
    super(product);
  }

  public boolean shouldShowEmptyLotWarning() {
    return this.shouldShowEmptyLotWarning;
  }

  public boolean hasLotChanged() {
    for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
      if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
        return true;
      }
    }
    for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
      if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean validate() {
    boolean valid = confirmedNoStockReceived || hasLotChanged() && validateNewLotList();
    if (!valid) {
      shouldShowEmptyLotWarning = true;
    }
    return valid;
  }
}
