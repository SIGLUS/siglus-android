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

package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joda.time.LocalDate;
import org.openlmis.core.enums.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodLotItem;
import org.openlmis.core.model.PodProduct;
import org.openlmis.core.network.model.LotResponse;
import org.openlmis.core.network.model.PodLotMovementItemResponse;
import org.openlmis.core.network.model.PodProductItemResponse;
import org.openlmis.core.network.model.PodResponse;
import org.openlmis.core.network.model.PodsLocalResponse;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class PodAdapter implements JsonDeserializer<PodsLocalResponse> {

  @Override
  public PodsLocalResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    final Gson gson = new Gson();
    final JsonArray jsonArray = json.getAsJsonArray();
    final List<PodResponse> podResponses = new ArrayList<>();
    for (JsonElement jsonElement : jsonArray) {
      podResponses.add(gson.fromJson(jsonElement, PodResponse.class));
    }
    final PodsLocalResponse podsLocalResponse = new PodsLocalResponse();
    podsLocalResponse.setPods(FluentIterable.from(podResponses).transform(podResponse -> {
      try {
        return fitForPod(Objects.requireNonNull(podResponse));
      } catch (LMISException e) {
        new LMISException(e, "PodAdapter.deserialize").reportToFabric();
        throw new JsonParseException("Pod deserialize fail", e);
      }
    }).toList());
    return podsLocalResponse;
  }

  private Pod fitForPod(PodResponse podResponse) throws LMISException {
    return Pod.builder()
        .shippedDate(new LocalDate(podResponse.getShippedDate()).toString())
        .orderCode(podResponse.getOrder().getCode())
        .orderSupplyFacilityName(podResponse.getOrder().getSupplyFacilityName())
        .orderStatus(mapToOrderStatus(podResponse.getOrder().getStatus()))
        .orderCreatedDate(new LocalDate(podResponse.getOrder().getCreatedDate()).toString())
        .orderLastModifiedDate(new LocalDate(podResponse.getOrder().getLastModifiedDate()).toString())
        .requisitionNumber(podResponse.getOrder().getRequisition().getNumber())
        .requisitionIsEmergency(podResponse.getOrder().getRequisition().isEmergency())
        .requisitionProgramCode(podResponse.getOrder().getRequisition().getProgramCode())
        .requisitionStartDate(new LocalDate(podResponse.getOrder().getRequisition().getStartDate()).toString())
        .requisitionEndDate(new LocalDate(podResponse.getOrder().getRequisition().getEndDate()).toString())
        .requisitionActualStartDate(
            new LocalDate(podResponse.getOrder().getRequisition().getActualStartDate()).toString())
        .requisitionActualEndDate(new LocalDate(podResponse.getOrder().getRequisition().getActualEndDate()).toString())
        .podProductsWrapper(buildPodProductItems(podResponse))
        .build();
  }

  private List<PodProduct> buildPodProductItems(PodResponse podResponse) {
    return FluentIterable.from(podResponse.getProducts()).transform(this::fitForPodProduct).toList();
  }

  private PodProduct fitForPodProduct(PodProductItemResponse podProductItemResponse) {
    return PodProduct.builder()
        .code(podProductItemResponse.getCode())
        .orderedQuantity(podProductItemResponse.getOrderedQuantity())
        .partialFulfilledQuantity(podProductItemResponse.getPartialFulfilledQuantity())
        .podLotItemsWrapper(buildPodLotItems(podProductItemResponse))
        .build();
  }

  private List<PodLotItem> buildPodLotItems(PodProductItemResponse podProductItemResponse) {
    return FluentIterable.from(podProductItemResponse.getLots()).transform(this::fitForPodLotItems).toList();
  }

  private PodLotItem fitForPodLotItems(PodLotMovementItemResponse podLotMovementItemResponse) {
    return PodLotItem.builder()
        .lot(buildLot(podLotMovementItemResponse.getLot()))
        .shippedQuantity(podLotMovementItemResponse.getShippedQuantity())
        .build();
  }

  private Lot buildLot(LotResponse lotResponse) {
    return Lot.builder()
        .lotNumber(lotResponse.getLotCode())
        .expirationDate(DateUtil.parseString(lotResponse.getExpirationDate(), DateUtil.DB_DATE_FORMAT))
        .build();
  }

  private OrderStatus mapToOrderStatus(String status) throws LMISException {
    for (OrderStatus orderStatus : OrderStatus.values()) {
      if (orderStatus.name().equalsIgnoreCase(status)) {
        return orderStatus;
      }
    }
    throw new LMISException("Illegal order status" + status);
  }
}
