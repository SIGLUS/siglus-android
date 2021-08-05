package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;

@Data
public class PodProductItemResponse {

  private String code;
  private long orderedQuantity;
  private long partialFulfilledQuantity;
  private List<PodLotMovementItemResponse> lots;

}
