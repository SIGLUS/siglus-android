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

import com.google.inject.Inject;
import java.util.List;
import lombok.Data;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.RoboGuice;

@Data
public class PodProductItemResponse {

  private String code;

  private Long orderedQuantity;

  private Long partialFulfilledQuantity;

  private List<PodLotMovementItemResponse> lots;

  @Inject
  public ProductRepository productRepository;

  public PodProductItemResponse() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
  }

  public PodProductItem from() {
    try {
      Product product = productRepository.getByCode(code);
      return PodProductItem.builder()
          .product(product)
          .orderedQuantity(orderedQuantity)
          .partialFulfilledQuantity(partialFulfilledQuantity)
          .podProductLotItemsWrapper(buildPodProductLotItems())
          .build();
    } catch (LMISException e) {
      new LMISException(e, "pod product convert").reportToFabric();
    }
    return null;
  }

  private List<PodProductLotItem> buildPodProductLotItems() {
    return FluentIterable.from(lots).transform(PodLotMovementItemResponse::from).toList();
  }
}
