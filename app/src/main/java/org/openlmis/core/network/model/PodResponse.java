package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;
import org.joda.time.LocalDate;

@Data
public class PodResponse {

  private PodOrderItemResponse order;
  private String  shippedDate;
  private List<PodProductItemResponse> products;
}
