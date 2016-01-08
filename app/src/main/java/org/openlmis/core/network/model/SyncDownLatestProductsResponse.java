package org.openlmis.core.network.model;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncDownLatestProductsResponse {
    List<ProductAndSupportedPrograms> latestProducts;
    String latestUpdatedTime;
}
