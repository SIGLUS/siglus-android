package org.openlmis.core.network.model;

import lombok.Data;

@Data
public class PodLotMovementItemResponse {

  private LotResponse lotResponse;
  private long shippedQuantity;
}
