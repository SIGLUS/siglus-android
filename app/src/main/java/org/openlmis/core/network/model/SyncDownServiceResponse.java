package org.openlmis.core.network.model;

import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.ServiceItem;

import java.util.List;

import lombok.Data;

@Data
public class SyncDownServiceResponse {
    List<Service> services;
}
