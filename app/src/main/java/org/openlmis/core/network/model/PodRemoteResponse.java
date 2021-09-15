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

import java.util.Date;
import java.util.List;
import lombok.Data;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
public class PodRemoteResponse {

  private PodOrderItemResponse order;

  private String shippedDate;

  private String deliveredBy;

  private String receivedBy;

  private String receivedDate;

  private List<PodProductItemResponse> products;

  public Pod from() throws LMISException {
    return Pod.builder()
        .shippedDate(DateUtil.parseString(shippedDate, DateUtil.DB_DATE_FORMAT))
        .deliveredBy(deliveredBy)
        .receivedBy(receivedBy)
        .receivedDate(receivedDate == null ? null : DateUtil.parseString(receivedDate, DateUtil.DB_DATE_FORMAT))
        .orderCode(order.getCode())
        .orderSupplyFacilityName(order.getSupplyFacilityName())
        .orderSupplyFacilityDistrict(order.getSupplyFacilityDistrict())
        .orderSupplyFacilityProvince(order.getSupplyFacilityProvince())
        .orderStatus(OrderStatus.covertToOrderStatus(order.getStatus()))
        .orderCreatedDate(new Date(order.getCreatedDate()))
        .orderLastModifiedDate(new Date(order.getLastModifiedDate()))
        .requisitionNumber(order.getRequisition().getNumber())
        .requisitionIsEmergency(order.getRequisition().isEmergency())
        .requisitionProgramCode(order.getRequisition().getProgramCode())
        .requisitionStartDate(DateUtil.parseString(order.getRequisition().getStartDate(), DateUtil.DB_DATE_FORMAT))
        .requisitionEndDate(DateUtil.parseString(order.getRequisition().getEndDate(), DateUtil.DB_DATE_FORMAT))
        .requisitionActualStartDate(
            DateUtil.parseString(order.getRequisition().getActualStartDate(), DateUtil.DB_DATE_FORMAT))
        .requisitionActualEndDate(
            DateUtil.parseString(order.getRequisition().getActualEndDate(), DateUtil.DB_DATE_FORMAT))
        .processedDate(DateUtil.parseString(order.getRequisition().getProcessedDate(), DateUtil.ISO_DATE_TIME_FORMAT))
        .serverProcessedDate(DateUtil.parseString(order.getRequisition().getServerProcessedDate(),
            DateUtil.ISO_DATE_TIME_FORMAT))
        .podProductItemsWrapper(buildPodProductItems())
        .build();
  }

  private List<PodProductItem> buildPodProductItems() {
    return FluentIterable.from(products).transform(PodProductItemResponse::from).toList();
  }
}
