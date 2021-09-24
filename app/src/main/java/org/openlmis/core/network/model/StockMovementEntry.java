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

package org.openlmis.core.network.model;

import static org.openlmis.core.manager.MovementReasonManager.MovementType.ADJUSTMENT;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.ISSUE;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.NEGATIVE_ADJUST;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.PHYSICAL_INVENTORY;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.POSITIVE_ADJUST;
import static org.openlmis.core.manager.MovementReasonManager.UNPACK_KIT;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
@NoArgsConstructor
public class StockMovementEntry {

  String processedDate;
  String signature;
  String occurredDate;
  String documentationNo;
  String productCode;
  String type;
  String reasonName;
  Long requested;
  long stockOnHand;
  long quantity;
  List<LotMovementEntry> lotEventList = new ArrayList<>();

  public StockMovementEntry(StockMovementItem stockMovementItem) {
    this.setProcessedDate(new DateTime(stockMovementItem.getCreatedTime()).toInstant().toString());
    this.setSignature(stockMovementItem.getSignature());
    this.setOccurredDate(DateUtil.formatDate(stockMovementItem.getMovementDate(), DateUtil.DB_DATE_FORMAT));
    this.setDocumentationNo(stockMovementItem.getDocumentNumber());
    this.setProductCode(stockMovementItem.getStockCard().getProduct().getCode());
    this.setType(getMovementType(stockMovementItem));
    this.setStockOnHand(stockMovementItem.getStockOnHand());
    this.setQuantity(getQuantityWithSign(stockMovementItem));
    this.setRequested(stockMovementItem.getRequested());
    if (stockMovementItem.getLotMovementItemListWrapper().isEmpty()) {
      this.setReasonName(this.type.equals(UNPACK_KIT) ? "" : stockMovementItem.getReason());
    } else {
      lotEventList.addAll(FluentIterable.from(stockMovementItem.getLotMovementItemListWrapper())
          .transform(LotMovementEntry::new)
          .toList());
    }
  }

  private long getQuantityWithSign(StockMovementItem stockMovementItem) {
    if (stockMovementItem.isNegativeMovement()) {
      return -stockMovementItem.getMovementQuantity();
    }
    return stockMovementItem.getMovementQuantity();
  }

  private String getMovementType(StockMovementItem stockMovementItem) {
    MovementType movementType = stockMovementItem.getMovementType();
    if (movementType == PHYSICAL_INVENTORY) {
      return PHYSICAL_INVENTORY.toString();
    } else if (movementType == NEGATIVE_ADJUST || movementType == POSITIVE_ADJUST) {
      return ADJUSTMENT.toString();
    } else if (isUnpack(stockMovementItem)) {
      return UNPACK_KIT;
    }
    return stockMovementItem.getMovementType().toString();
  }

  private boolean isUnpack(StockMovementItem stockMovementItem) {
    return stockMovementItem.getMovementType() == ISSUE
        && stockMovementItem.getReason().equalsIgnoreCase(UNPACK_KIT);
  }
}
