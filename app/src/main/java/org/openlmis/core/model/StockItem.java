package org.openlmis.core.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@Data
@DatabaseTable(tableName = "stock_items")
public class StockItem {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    String documentNumber;

    @DatabaseField
    int amount;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    AdjustmentReason reason;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    StockCard stockCard;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Product product;
}
