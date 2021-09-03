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

package org.openlmis.core.model.builder;

import java.util.Date;
import java.util.List;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

public class PodBuilder {

  public static Pod generatePod() throws Exception {
    ProductRepository productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    Product product = ProductBuilder.buildAdultProduct();
    productRepository.createOrUpdate(product);
    Lot lot = new LotBuilder()
        .setProduct(product)
        .setLotNumber(FieldConstants.LOT_NUMBER)
        .setExpirationDate(new Date())
        .build();
    Pod pod = Pod.builder()
        .orderCode(FieldConstants.ORDER_CODE)
        .orderStatus(OrderStatus.SHIPPED)
        .build();
    List<PodProductItem> podProductItemsWrapper = pod.getPodProductItemsWrapper();
    Long quantity = 10L;
    PodProductItem podProductItem = PodProductItem.builder()
        .pod(pod)
        .product(product)
        .orderedQuantity(quantity)
        .partialFulfilledQuantity(5L)
        .build();
    List<PodProductLotItem> podProductLotItemsWrapper = podProductItem.getPodProductLotItemsWrapper();
    PodProductLotItem podProductLotItem = PodProductLotItem.builder()
        .podProductItem(podProductItem)
        .lot(lot)
        .shippedQuantity(quantity)
        .build();
    podProductLotItemsWrapper.add(podProductLotItem);
    podProductItemsWrapper.add(podProductItem);
    return pod;
  }
}
