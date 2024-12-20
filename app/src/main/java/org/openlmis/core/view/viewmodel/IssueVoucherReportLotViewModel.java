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

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

@Data
@NoArgsConstructor
public class IssueVoucherReportLotViewModel {

  private Lot lot;
  private Long shippedQuantity;
  private Long acceptedQuantity;
  private String rejectedReason;
  private String notes;
  private PodProductLotItem lotItem;
  private OrderStatus orderStatus;
  private boolean isLocal;
  private boolean isDraft;
  private boolean isValidate = true;

  private boolean isAdded;

  public IssueVoucherReportLotViewModel(PodProductLotItem lotItem, PodProductItem podProductItem,
      OrderStatus orderStatus, boolean isLocal, boolean isDraft) {
    this.isDraft = isDraft;
    this.isLocal = isLocal;
    this.lotItem = lotItem;
    lot = buildLot(lotItem, podProductItem);
    shippedQuantity = lotItem.getShippedQuantity();
    acceptedQuantity = lotItem.getAcceptedQuantity();
    rejectedReason = lotItem.getRejectedReason();
    notes = lotItem.getNotes();
    this.orderStatus = orderStatus;
    isAdded = lotItem.isAdded();
  }

  public IssueVoucherReportLotViewModel(
      Lot lot, String rejectedReason, OrderStatus orderStatus,
      long shippedQuantity, PodProductLotItem lotItem, boolean isAdded
  ) {
    this.lot = lot;
    this.rejectedReason = rejectedReason;
    this.orderStatus = orderStatus;
    this.shippedQuantity = shippedQuantity;
    this.lotItem = lotItem;
    this.isAdded = isAdded;
  }

  public Long compareAcceptedAndShippedQuantity() {
    if (shippedQuantity == null || acceptedQuantity == null) {
      return null;
    }
    return acceptedQuantity - shippedQuantity;
  }

  public String getRejectionReasonDesc(boolean useDefault) {
    MovementReason reason = null;
    try {
      reason = MovementReasonManager.getInstance()
          .queryByCode(MovementType.REJECTION, rejectedReason);
    } catch (Exception ignored) {
      // do nothing
    }
    if (reason == null) {
      return useDefault ? LMISApp.getInstance().getString(R.string.label_default_rejection_reason)
          : "";
    } else {
      return reason.getDescription();
    }
  }

  public PodProductLotItem convertToModel() {
    lotItem.setShippedQuantity(shippedQuantity);
    lotItem.setAcceptedQuantity(acceptedQuantity);
    lotItem.setRejectedReason(rejectedReason);
    lotItem.setNotes(notes);
    lotItem.setLot(lot);
    lotItem.setAdded(isAdded);

    return lotItem;
  }

  public BigDecimal getTotalValue() {
    String price = null;
    if (lot != null) {
      price = lot.getProduct().getPrice();
    }
    if (price == null || shippedQuantity == null) {
      return null;
    }
    BigDecimal perPrice = new BigDecimal(price);
    return perPrice.multiply(BigDecimal.valueOf(shippedQuantity));
  }

  public boolean shouldShowLotClear() {
    if (this.getLot() == null) {
      return false;
    }
    return this.isLocal() && this.isDraft() && !this.getLot().getProduct().isKit();
  }

  public boolean isAddedAndShipped() {
    return isAdded && orderStatus == OrderStatus.SHIPPED;
  }

  private Lot buildLot(PodProductLotItem podProductLotItem, PodProductItem podProductItem) {
    if (podProductItem.getProduct().isKit()) {
      return Lot.builder()
          .lotNumber(Constants.VIRTUAL_LOT_NUMBER)
          .expirationDate(DateUtil.parseString(DateUtil.getVirtualLotExpireDate(),
              DateUtil.DB_DATE_FORMAT))
          .product(podProductItem.getProduct())
          .build();
    } else {
      return podProductLotItem.getLot();
    }
  }

}
