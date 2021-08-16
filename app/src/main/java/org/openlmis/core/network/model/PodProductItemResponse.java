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
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
public class PodProductItemResponse {

  private String code;

  private long orderedQuantity;

  private long partialFulfilledQuantity;

  private List<PodLotMovementItemResponse> lots;

  public PodProductItem from() {
    return PodProductItem.builder()
        .code(code)
        .orderedQuantity(orderedQuantity)
        .partialFulfilledQuantity(partialFulfilledQuantity)
        .podProductLotItemsWrapper(buildPodProductLotItems())
        .build();
  }

  private List<PodProductLotItem> buildPodProductLotItems() {
    return FluentIterable.from(lots).transform(PodLotMovementItemResponse::from).toList();
  }
}
