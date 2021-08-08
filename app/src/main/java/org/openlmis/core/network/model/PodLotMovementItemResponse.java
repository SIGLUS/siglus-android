package org.openlmis.core.network.model;

import lombok.Data;

@Data
public class PodLotMovementItemResponse {

  private LotResponse lot;
  private long shippedQuantity;
}
