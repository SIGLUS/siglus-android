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

import java.util.List;
import lombok.Data;
import org.joda.time.LocalDate;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
public class PodResponse {

  private PodOrderItemResponse order;

  private String  shippedDate;

  private List<PodProductItemResponse> products;

  public Pod from() throws LMISException {
    return Pod.builder()
        .shippedDate(new LocalDate(shippedDate).toString())
        .orderCode(order.getCode())
        .orderSupplyFacilityName(order.getSupplyFacilityName())
        .orderStatus(OrderStatus.covertToOrderStatus(order.getStatus()))
        .orderCreatedDate(new LocalDate(order.getCreatedDate()).toString())
        .orderLastModifiedDate(new LocalDate(order.getLastModifiedDate()).toString())
        .requisitionNumber(order.getRequisition().getNumber())
        .requisitionIsEmergency(order.getRequisition().isEmergency())
        .requisitionProgramCode(order.getRequisition().getProgramCode())
        .requisitionStartDate(new LocalDate(order.getRequisition().getStartDate()).toString())
        .requisitionEndDate(new LocalDate(order.getRequisition().getEndDate()).toString())
        .requisitionActualStartDate(new LocalDate(order.getRequisition().getActualStartDate()).toString())
        .requisitionActualEndDate(new LocalDate(order.getRequisition().getActualEndDate()).toString())
        .podProductItemsWrapper(buildPodProductItems())
        .build();
  }

  private List<PodProductItem> buildPodProductItems() {
    return FluentIterable.from(products).transform(PodProductItemResponse::from).toList();
  }
}
