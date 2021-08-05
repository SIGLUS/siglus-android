package org.openlmis.core.network.model;

import lombok.Data;
import org.joda.time.LocalDate;

@Data
public class PodRequisitionItemResponse {

  private String number;
  private boolean isEmergency;
  private String programCode;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalDate actualStartDate;
  private LocalDate actualEndDate;


}
