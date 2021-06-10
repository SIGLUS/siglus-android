package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;
import org.openlmis.core.model.ReportTypeForm;

@Data
public class SyncDownReportTypeResponse {

  List<ReportTypeForm> reportTypes;
}
