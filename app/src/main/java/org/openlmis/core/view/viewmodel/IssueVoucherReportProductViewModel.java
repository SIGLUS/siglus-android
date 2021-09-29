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

import java.util.List;
import lombok.Data;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.Product;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
public class IssueVoucherReportProductViewModel {

  private Product product;
  private String productUnitName;
  private String orderedQuantity;
  private String partialFulfilledQuantity;
  private OrderStatus orderStatus;
  private PodProductItem podProductItem;
  private List<IssueVoucherReportLotViewModel> lotViewModelList;
  private boolean isLocal;
  private boolean isDraft;
  private boolean isValidate = true;

  public IssueVoucherReportProductViewModel(PodProductItem podProductItem,
      OrderStatus orderStatus, boolean isLocal, boolean isDraft) {
    this.isDraft = isDraft;
    this.isLocal = isLocal;
    this.podProductItem = podProductItem;
    product = podProductItem.getProduct();
    productUnitName = product.getStrength() == null || product.getStrength().isEmpty() ? "each" : product.getStrength();
    orderedQuantity = podProductItem.getOrderedQuantity() == null ? ""
        : String.valueOf(podProductItem.getOrderedQuantity());
    partialFulfilledQuantity = podProductItem.getPartialFulfilledQuantity() == null ? ""
        : String.valueOf(podProductItem.getPartialFulfilledQuantity());
    this.orderStatus = orderStatus;
    lotViewModelList = FluentIterable.from(podProductItem.getPodProductLotItemsWrapper())
        .transform(podLotItem -> new IssueVoucherReportLotViewModel(podLotItem, orderStatus, isLocal, isDraft))
        .toList();
  }

  public PodProductItem convertToPodProductModel() {
    List<PodProductLotItem> lotItems = FluentIterable.from(lotViewModelList).transform(lotViewModel -> {
      PodProductLotItem lotItem = lotViewModel.convertToModel();
      lotItem.setPodProductItem(podProductItem);
      return lotItem;
    }).toList();
    podProductItem.setPodProductLotItemsWrapper(lotItems);
    return podProductItem;
  }

  public PodProductItem restoreToPodProductModelForRemote() {
    List<PodProductLotItem> lotItems = FluentIterable.from(lotViewModelList).transform(lotViewModel -> {
      PodProductLotItem lotItem = lotViewModel.convertToModel();
      lotItem.setPodProductItem(podProductItem);
      lotItem.setAcceptedQuantity(null);
      lotItem.setRejectedReason(null);
      lotItem.setNotes(null);
      return lotItem;
    }).toList();
    podProductItem.setPodProductLotItemsWrapper(lotItems);
    return podProductItem;
  }

  public boolean validate() {
    for (IssueVoucherReportLotViewModel lotViewModel : lotViewModelList) {
      if (lotViewModel.getOrderStatus() == OrderStatus.SHIPPED && isContainInvalidateQuantity(lotViewModel)) {
        setValidate(false);
        return false;
      }
    }
    setValidate(true);
    return true;
  }


  public void setValidate(boolean isValidate) {
    this.isValidate = isValidate;
    if (lotViewModelList == null || lotViewModelList.isEmpty()) {
      return;
    }
    for (IssueVoucherReportLotViewModel lotViewModel : lotViewModelList) {
      lotViewModel.setValidate(isValidate);
    }
  }

  private boolean isContainInvalidateQuantity(IssueVoucherReportLotViewModel lotViewModel) {
    return lotViewModel.getShippedQuantity() == null || lotViewModel.getAcceptedQuantity() == null
        || lotViewModel.getReturnedQuality() < 0 || isInvalidateReason(lotViewModel);
  }

  private boolean isInvalidateReason(IssueVoucherReportLotViewModel lotViewModel) {
    return lotViewModel.getReturnedQuality() != null && lotViewModel.getReturnedQuality() > 0
        && lotViewModel.getRejectedReason() == null;
  }

}
