package org.openlmis.core.model;


import java.util.List;

import lombok.Data;

@Data
public class StockCard {
    String id;
    Product product;
    List<StockItem> stockItemList;
}
