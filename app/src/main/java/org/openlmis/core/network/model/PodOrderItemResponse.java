package org.openlmis.core.network.model;

import lombok.Data;

@Data
public class PodOrderItemResponse {

  private String code;
  private String supplyFacilityName;
  private String status;
  private long createdDate;
  private long lastModifiedDate;
  private PodRequisitionItemResponse podRequisitionItemResponse;

}
