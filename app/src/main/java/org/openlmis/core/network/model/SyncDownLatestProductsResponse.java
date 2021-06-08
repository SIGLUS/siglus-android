package org.openlmis.core.network.model;

import java.util.List;

import lombok.Data;

@Data
public class SyncDownLatestProductsResponse {
    List<ProductAndSupportedPrograms> latestProducts;
    String lastSyncTime;
}
