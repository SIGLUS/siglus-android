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
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.PodProductLotItem;

@Data
public class IssueVoucherReportLotViewModel {

  private Lot lot;
  private Long shippedQuantity;
  private Long acceptedQuantity;
  private String rejectedReason;
  private String notes;
  private PodProductLotItem lotItem;
  private OrderStatus orderStatus;
  private boolean isLocal;
  private boolean isValidate = true;

  public IssueVoucherReportLotViewModel(PodProductLotItem lotItem, OrderStatus orderStatus, boolean isLocal) {
    this.isLocal = isLocal;
    this.lotItem = lotItem;
    lot = lotItem.getLot();
    shippedQuantity = lotItem.getShippedQuantity();
    acceptedQuantity = lotItem.getAcceptedQuantity();
    rejectedReason = lotItem.getRejectedReason();
    notes = lotItem.getNotes();
    this.orderStatus = orderStatus;
  }

  public Long getReturnedQuality() {
    if (shippedQuantity == null || acceptedQuantity == null) {
      return null;
    }
    return shippedQuantity.longValue() - acceptedQuantity.longValue();
  }

  public PodProductLotItem convertToModel() {
    lotItem.setShippedQuantity(shippedQuantity);
    lotItem.setAcceptedQuantity(acceptedQuantity);
    lotItem.setRejectedReason(rejectedReason);
    lotItem.setNotes(notes);
    return lotItem;
  }

}