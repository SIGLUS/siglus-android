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

import lombok.Data;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.PodProductLotItem;

@Data
public class PodLotMovementItemResponse {

  private LotResponse lot;

  private Long shippedQuantity;

  private Long acceptedQuantity;

  private String rejectedReason;

  private String notes;

  public PodProductLotItem from() {
    return PodProductLotItem.builder()
        .lot(buildLot())
        .shippedQuantity(shippedQuantity)
        .acceptedQuantity(acceptedQuantity)
        .rejectedReason(rejectedReason)
        .notes(notes)
        .build();
  }

  private Lot buildLot() {
    if (lot != null) {
      return lot.from();
    }
    return null;
  }
}
