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

import com.chad.library.adapter.base.entity.MultiItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.openlmis.core.model.DraftIssueVoucherProductItem;
import org.openlmis.core.model.DraftIssueVoucherProductLotItem;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.DateUtil;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueVoucherLotViewModel implements MultiItemEntity, Comparable<IssueVoucherLotViewModel> {

  public static final int TYPE_EDIT = 1;

  public static final int TYPE_DONE = 2;

  public static final int TYPE_KIT_EDIT = 3;

  public static final int TYPE_KIT_DONE = 4;

  private Long shippedQuantity;

  private Long acceptedQuantity;

  private boolean done;

  private String lotNumber;

  private String expiryDate;

  @Getter
  private Product product;

  private boolean isNewAdd;

  private boolean valid;

  private boolean shouldShowError;

  private Lot lot;

  private DraftIssueVoucherProductLotItem productLotItem;

  public IssueVoucherLotViewModel(String lotNumber, String expiryDate, Product product) {
    this.lotNumber = lotNumber;
    this.expiryDate = expiryDate;
    this.product = product;
    this.isNewAdd = true;
    this.valid = true;
    this.shouldShowError = false;
    this.lot = Lot.builder()
        .lotNumber(lotNumber)
        .expirationDate(DateUtil.parseString(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
        .product(product)
        .build();
  }

  public static IssueVoucherLotViewModel build(LotOnHand lotOnHand) {
    return new IssueVoucherLotViewModelBuilder()
        .lotNumber(lotOnHand.getLot().getLotNumber())
        .expiryDate(DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(),
            DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
        .product(lotOnHand.getStockCard().getProduct())
        .lot(lotOnHand.getLot())
        .valid(true)
        .isNewAdd(false)
        .shouldShowError(false)
        .build();
  }

  @Override
  public int getItemType() {
    if (done) {
      return isVirtualLot() ? TYPE_KIT_DONE : TYPE_DONE;
    }
    return isVirtualLot() ? TYPE_KIT_EDIT : TYPE_EDIT;
  }

  public boolean isLotAllBlank() {
    return shippedQuantity == null && acceptedQuantity == null;
  }

  public boolean validateLot() {
    return !isAcceptedQuantityMoreThanShippedQuantity()
        && !isNewAddedLotHasBlank()
        && !existedLotHasBlank()
        && !isShippedQuantityZero();
  }

  public boolean isAcceptedQuantityMoreThanShippedQuantity() {
    return shippedQuantity != null
        && acceptedQuantity != null
        && acceptedQuantity > shippedQuantity;
  }

  public boolean isNewAddedLotHasBlank() {
    return isNewAdd
        && (shippedQuantity == null || acceptedQuantity == null);
  }

  public boolean existedLotHasBlank() {
    return !isNewAdd
        && ((shippedQuantity != null && acceptedQuantity == null)
        || (shippedQuantity == null && acceptedQuantity != null));
  }

  public boolean isShippedQuantityZero() {
    return shippedQuantity != null && shippedQuantity == 0;
  }

  public PodProductLotItem from() {
    return PodProductLotItem.builder()
        .shippedQuantity(shippedQuantity)
        .acceptedQuantity(acceptedQuantity)
        .lot(lot)
        .build();
  }

  public DraftIssueVoucherProductLotItem covertToDraft(DraftIssueVoucherProductItem productItem) {
    return DraftIssueVoucherProductLotItem.builder()
        .draftIssueVoucherProductItem(productItem)
        .shippedQuantity(shippedQuantity)
        .acceptedQuantity(acceptedQuantity)
        .done(done)
        .expirationDate(DateUtil.getActualMaximumDate(DateUtil
            .parseString(expiryDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)))
        .lotNumber(lotNumber)
        .newAdded(isNewAdd)
        .build();
  }

  public boolean hasChanged() {
    return ObjectUtils.notEqual(shippedQuantity, productLotItem.getShippedQuantity())
        || ObjectUtils.notEqual(acceptedQuantity, productLotItem.getAcceptedQuantity());
  }

  public boolean isVirtualLot() {
    return this.getProduct().isKit();
  }

  @Override
  public int compareTo(IssueVoucherLotViewModel another) {
    if (this.isNewAdd) {
      return 1;
    } else {
      return -1;
    }
  }
}
