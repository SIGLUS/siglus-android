package org.openlmis.core.view.viewmodel;

import lombok.Builder;
import lombok.Data;
import org.openlmis.core.model.Pod;

@Data
@Builder
public class IssueVoucherReportViewModel {
  private Pod pod;

}
