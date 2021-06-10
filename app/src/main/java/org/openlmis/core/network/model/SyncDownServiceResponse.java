package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;
import org.openlmis.core.model.Service;

@Data
public class SyncDownServiceResponse {

  List<Service> latestServices;
  String latestUpdatedTime;
}
