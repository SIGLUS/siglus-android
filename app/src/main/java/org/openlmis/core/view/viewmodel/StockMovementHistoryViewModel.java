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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;

@Data
public class StockMovementHistoryViewModel {

  private String movementDate;
  private String requested;
  private String stockOnHand;
  private String signature;
  private StockMovementItem stockMovementItem;
  private List<LotMovementHistoryViewModel> lotViewModelList = new ArrayList<>();

  public StockMovementHistoryViewModel(StockMovementItem item) {
    stockMovementItem = item;
    movementDate = DateUtil.formatDate(item.getMovementDate());
    stockOnHand = String.valueOf(item.getStockOnHand());
    signature = item.getSignature();
    requested = item.getRequested() == null ? "" : String.valueOf(item.getRequested());
    buildLotViewModelList(item);
  }

  public boolean isNoStock() {
    MovementType movementType = stockMovementItem.getMovementType();
    String movementReason = stockMovementItem.getReason();
    return (movementType == MovementType.INITIAL_INVENTORY || movementType == MovementType.PHYSICAL_INVENTORY)
        && stockMovementItem.getStockOnHand() == 0
        && stockMovementItem.getLotMovementItemListWrapper().isEmpty()
        && MovementReasonManager.INVENTORY.equalsIgnoreCase(movementReason);
  }

  public boolean needShowRed() {
    MovementType movementType = stockMovementItem.getMovementType();
    String movementReason = stockMovementItem.getReason();
    return movementType == MovementType.INITIAL_INVENTORY
        || movementType == MovementType.PHYSICAL_INVENTORY
        || movementType == MovementType.RECEIVE
        || (movementType == MovementType.ISSUE && MovementReasonManager.UNPACK_KIT.equalsIgnoreCase(movementReason));
  }

  private void buildLotViewModelList(StockMovementItem item) {
    lotViewModelList.clear();
    for (LotMovementItem lotMovementItem : item.getLotMovementItemListWrapper()) {
      if (lotMovementItem.isUselessMovement()) {
        continue;
      }
      lotViewModelList.add(new LotMovementHistoryViewModel(item.getMovementType(), lotMovementItem));
    }
    Collections.sort(lotViewModelList);
  }
}
