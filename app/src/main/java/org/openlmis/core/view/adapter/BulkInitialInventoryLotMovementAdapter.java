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

package org.openlmis.core.view.adapter;

import java.util.List;
import org.openlmis.core.view.activity.BulkInitialInventoryActivity;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

public class BulkInitialInventoryLotMovementAdapter extends LotMovementAdapter {

  public BulkInitialInventoryLotMovementAdapter(List<LotMovementViewModel> data) {
    super(data);
  }

  public BulkInitialInventoryLotMovementAdapter(List<LotMovementViewModel> data,
      String productName) {
    super(data, productName);
  }

  public int validateLotNonEmptyQuantity() {
    int position = -1;
    for (LotMovementViewModel lotMovementViewModel : lotList) {
      lotMovementViewModel.setValid(true);
      lotMovementViewModel.setQuantityLessThanSoh(true);
    }
    for (int i = 0; i < lotList.size(); i++) {
      if (!lotList.get(i).validateLotWithNoEmptyFields()) {
        position = i;
        break;
      }
    }

    this.notifyDataSetChanged();
    return position;
  }

  public int validateLotPositiveQuantity() {
    int position = -1;
    for (LotMovementViewModel lotMovementViewModel : lotList) {
      lotMovementViewModel.setValid(true);
      lotMovementViewModel.setQuantityLessThanSoh(true);
      lotMovementViewModel.setFrom(BulkInitialInventoryActivity.KEY_FROM_INITIAL_INVENTORY);
    }
    for (int i = 0; i < lotList.size(); i++) {
      if (!lotList.get(i).validateLotWithPositiveQuantity()) {
        position = i;
        break;
      }
    }

    this.notifyDataSetChanged();
    return position;
  }
}
