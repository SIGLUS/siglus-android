package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import org.joda.time.LocalDateTime;
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
import org.openlmis.core.network.model.SyncDownPodResponse;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class PodAdapter implements JsonDeserializer<PodsLocalResponse> {

  @Override
  public PodsLocalResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    final Gson gson = new Gson();
    final SyncDownPodResponse syncDownPodResponse = gson.fromJson(json, SyncDownPodResponse.class);
    final PodsLocalResponse podsLocalResponse = new PodsLocalResponse();
    podsLocalResponse.getPods()
        .addAll(FluentIterable.from(syncDownPodResponse.getPodResponses()).transform(podResponse -> {
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
        .shippedDate(podResponse.getShippedDate())
        .orderCode(podResponse.getPodOrderItemResponse().getCode())
        .orderSupplyFacilityName(podResponse.getPodOrderItemResponse().getSupplyFacilityName())
        .orderStatus(mapToOrderStatus(podResponse.getPodOrderItemResponse().getStatus()))
        .orderCreatedDate(new LocalDateTime(podResponse.getPodOrderItemResponse().getCreatedDate()))
        .orderLastModifiedDate(new LocalDateTime(podResponse.getPodOrderItemResponse().getLastModifiedDate()))
        .requisitionNumber(podResponse.getPodOrderItemResponse().getPodRequisitionItemResponse().getNumber())
        .requisitionIsEmergency(podResponse.getPodOrderItemResponse().getPodRequisitionItemResponse().isEmergency())
        .requisitionProgramCode(podResponse.getPodOrderItemResponse().getPodRequisitionItemResponse().getProgramCode())
        .requisitionStartDate(podResponse.getPodOrderItemResponse().getPodRequisitionItemResponse().getStartDate())
        .requisitionEndDate(podResponse.getPodOrderItemResponse().getPodRequisitionItemResponse().getEndDate())
        .requisitionActualStartDate(
            podResponse.getPodOrderItemResponse().getPodRequisitionItemResponse().getActualStartDate())
        .requisitionActualEndDate(
            podResponse.getPodOrderItemResponse().getPodRequisitionItemResponse().getActualEndDate())
        .podProductsWrapper(buildPodProductItems(podResponse))
        .build();
  }

  private List<PodProduct> buildPodProductItems(PodResponse podResponse) {
    return FluentIterable.from(podResponse.getPodProductItemResponses()).transform(this::fitForPodProduct).toList();
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
        .lot(buildLot(podLotMovementItemResponse.getLotResponse()))
        .shippedQuantity(podLotMovementItemResponse.getShippedQuantity())
        .build();
  }

  private Lot buildLot(LotResponse lotResponse) {
    return Lot.builder()
        .lotNumber(lotResponse.getLotCode())
        .expirationDate(DateUtil.parseString(lotResponse.getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
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
