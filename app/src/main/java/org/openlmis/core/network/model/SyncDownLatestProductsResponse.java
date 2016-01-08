package org.openlmis.core.network.model;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SyncDownLatestProductsResponse {
    List<ProductAndSupportedPrograms> productsAndSupportedPrograms;
    String latestUpdatedTime;
}
