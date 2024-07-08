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

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.core.enumeration.IssueVoucherItemType;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.Product;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
@NoArgsConstructor
public class IssueVoucherReportProductViewModel implements MultiItemEntity {

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
        .transform(podLotItem -> new IssueVoucherReportLotViewModel(podLotItem, podProductItem, orderStatus, isLocal,
            isDraft))
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

  public boolean shouldShowProductClear() {
    return this.isLocal() && this.isDraft();
  }

  private boolean isContainInvalidateQuantity(IssueVoucherReportLotViewModel lotViewModel) {
    return lotViewModel.getShippedQuantity() == null
        || lotViewModel.getAcceptedQuantity() == null
        || isInvalidateReason(lotViewModel);
  }

  private boolean isInvalidateReason(IssueVoucherReportLotViewModel lotViewModel) {
    Long diffQuantity = lotViewModel.compareAcceptedAndShippedQuantity();

    return diffQuantity != null && diffQuantity != 0 && lotViewModel.getRejectedReason() == null;
  }

  @Override
  public int getItemType() {
    return IssueVoucherItemType.ISSUE_VOUCHER_PRODUCT_TYPE.getValue();
  }

  public List<String> getLotNumbers() {
      List<IssueVoucherReportLotViewModel> lotViewModelList = getLotViewModelList();
      if (lotViewModelList != null) {
        return from(lotViewModelList)
            .filter(lotViewModel -> lotViewModel != null && lotViewModel.getLot() != null)
            .transform(lotViewModel -> lotViewModel.getLot().getLotNumber())
            .toList();
      }
    return null;
  }

  public void addNewLot(
      String lotNumber, Date expirationDate,
      String rejectedReasonCode, String rejectedReasonDescription,
      OrderStatus orderStatus, long shippedQuantity
  ) {
    // check duplicated lot number
    if (lotViewModelList != null) {
      IssueVoucherReportLotViewModel sameLotNumberViewModel = from(lotViewModelList)
          .firstMatch(
              viewModel -> viewModel != null && lotNumber.equals(viewModel.getLot().getLotNumber())
          )
          .orNull();

      if (sameLotNumberViewModel != null) {
        return;
      }
    } else {
      lotViewModelList = new ArrayList<>();
    }
    // build new lotViewModel
    Lot lot = new Lot();
    lot.setLotNumber(lotNumber);
    lot.setProduct(getProduct());
    lot.setExpirationDate(expirationDate);

    IssueVoucherReportLotViewModel newLotViewModel = new IssueVoucherReportLotViewModel(
        lot, rejectedReasonCode, rejectedReasonDescription, orderStatus, shippedQuantity, new PodProductLotItem(), true
    );
    // add new lotViewModel
    ArrayList<IssueVoucherReportLotViewModel> newLotViewModelList = new ArrayList<>(
        lotViewModelList);
    newLotViewModelList.add(newLotViewModel);

    setLotViewModelList(newLotViewModelList);
  }

  public boolean isRemoteAndShipped() {
    return !isLocal && orderStatus == OrderStatus.SHIPPED;
  }
}
