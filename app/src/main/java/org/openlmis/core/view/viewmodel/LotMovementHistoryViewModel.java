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

import java.util.EnumMap;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.LotMovementItem;

@Data
@NoArgsConstructor
public class LotMovementHistoryViewModel implements Comparable<LotMovementHistoryViewModel> {

  private String stockOnHand;
  private String documentNo;
  private String lotCode;
  private LotMovementItem lotMovementItem;
  private MovementType stockCardMovementType;
  private MovementReasonManager.MovementReason reason;
  private EnumMap<MovementType, String> typeQuantityMap = new EnumMap<>(MovementType.class);

  public LotMovementHistoryViewModel(MovementType type, LotMovementItem item) {
    lotMovementItem = item;
    stockCardMovementType = type;
    documentNo = item.getDocumentNumber();
    lotCode = item.getLot().getLotNumber();
    stockOnHand = String.valueOf(item.getStockOnHand());
    try {
      reason = MovementReasonManager.getInstance().queryByCode(item.getReason());
    } catch (MovementReasonNotFoundException e) {
      throw new IllegalArgumentException("MovementReason Cannot be find " + e.getMessage(), e);
    }
    typeQuantityMap.put(type, String.valueOf(Math.abs(item.getMovementQuantity())));
  }

  public String getReceived() {
    return typeQuantityMap.get(MovementType.RECEIVE);
  }

  public String getIssued() {
    return typeQuantityMap.get(MovementType.ISSUE);
  }

  public String getNegativeAdjustment() {
    return typeQuantityMap.get(MovementType.NEGATIVE_ADJUST);
  }

  public String getPositiveAdjustment() {
    return typeQuantityMap.get(MovementType.POSITIVE_ADJUST);
  }

  public boolean needShowRed() {
    String movementReason = reason.getCode();
    return stockCardMovementType == MovementType.INITIAL_INVENTORY
        || stockCardMovementType == MovementType.PHYSICAL_INVENTORY
        || stockCardMovementType == MovementType.RECEIVE
        || (stockCardMovementType == MovementType.ISSUE && MovementReasonManager.UNPACK_KIT
        .equalsIgnoreCase(movementReason));
  }

  @Override
  public int compareTo(LotMovementHistoryViewModel other) {
    long otherExpirationDate = other.getLotMovementItem().getLot().getExpirationDate().getTime();
    long myExpirationDate = lotMovementItem.getLot().getExpirationDate().getTime();
    return Long.compare(myExpirationDate, otherExpirationDate);
  }
}