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

import static org.openlmis.core.manager.MovementReasonManager.INVENTORY_NEGATIVE;
import static org.openlmis.core.manager.MovementReasonManager.INVENTORY_POSITIVE;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.NEGATIVE_ADJUST;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.POSITIVE_ADJUST;

import java.util.EnumMap;
import java.util.Map;
import org.openlmis.core.manager.MovementReasonManager.MovementType;

public class MovementQuantityHelper {

  public static Map<MovementType, String> generateTypeQuantityMap(MovementType type, String reason,
      long movementQuantity) {
    EnumMap<MovementType, String> typeQuantityMap = new EnumMap<>(MovementType.class);
    if (isNegativeAdjustment(type, reason)) {
      typeQuantityMap.put(NEGATIVE_ADJUST, String.valueOf(movementQuantity));
    } else if (isPositiveAdjustment(type, reason)) {
      typeQuantityMap.put(POSITIVE_ADJUST, String.valueOf(movementQuantity));
    } else {
      typeQuantityMap.put(type, String.valueOf(movementQuantity));
    }
    return typeQuantityMap;
  }

  private static boolean isNegativeAdjustment(MovementType type, String reason) {
    return NEGATIVE_ADJUST == type
        || (MovementType.PHYSICAL_INVENTORY == type && INVENTORY_NEGATIVE.equalsIgnoreCase(reason));
  }

  private static boolean isPositiveAdjustment(MovementType type, String reason) {
    return POSITIVE_ADJUST == type
        || (MovementType.PHYSICAL_INVENTORY == type && INVENTORY_POSITIVE.equalsIgnoreCase(reason));
  }
}
