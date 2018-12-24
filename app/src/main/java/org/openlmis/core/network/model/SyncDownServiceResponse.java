package org.openlmis.core.network.model;

import org.openlmis.core.model.Service;

import java.util.List;

import lombok.Data;

@Data
public class SyncDownServiceResponse {
    List<Service> latestServices;
    String latestUpdatedTime;
}
