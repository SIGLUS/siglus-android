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
import org.openlmis.core.model.Product;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
public class IssueVoucherReportProductViewModel {

  private Product product;
  private String orderedQuantity;
  private String partialFulfilledQuantity;
  private OrderStatus orderStatus;
  private PodProductItem podProductItem;
  private List<IssueVoucherReportLotViewModel> lotViewModelList;

  public IssueVoucherReportProductViewModel(PodProductItem podProductItem,
      OrderStatus orderStatus) {
    this.podProductItem = podProductItem;
    product = podProductItem.getProduct();
    orderedQuantity = podProductItem.getOrderedQuantity() == null ? ""
        : String.valueOf(podProductItem.getOrderedQuantity());
    partialFulfilledQuantity = podProductItem.getPartialFulfilledQuantity() == null ? ""
        : String.valueOf(podProductItem.getPartialFulfilledQuantity());
    this.orderStatus = orderStatus;
    lotViewModelList = FluentIterable.from(podProductItem.getPodProductLotItemsWrapper())
        .transform(podLotItem -> new IssueVoucherReportLotViewModel(podLotItem, orderStatus))
        .toList();
  }

}
