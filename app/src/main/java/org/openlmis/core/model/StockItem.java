package org.openlmis.core.model;


import lombok.Data;

@Data
public class StockItem {
    String documentNumber;
    int amount;
    AdjustmentReason reason;
}
