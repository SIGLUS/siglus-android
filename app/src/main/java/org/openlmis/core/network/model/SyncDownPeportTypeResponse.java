package org.openlmis.core.network.model;

import org.openlmis.core.model.ReportTypeForm;

import java.util.List;

import lombok.Data;

@Data
public class SyncDownPeportTypeResponse {
    List<ReportTypeForm> reportTypes;
}
