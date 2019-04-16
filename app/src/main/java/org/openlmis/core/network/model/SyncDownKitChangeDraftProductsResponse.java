package org.openlmis.core.network.model;

import java.util.List;

import lombok.Data;

@Data
public class SyncDownKitChangeDraftProductsResponse {
    List<ProductAndSupportedPrograms> latestProducts;
    String latestUpdatedTime;
}