package org.openlmis.core.model;


import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

import lombok.Data;

@Data
@DatabaseTable(tableName = "stock_cards")
public class StockCard {


    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    String stockCardId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Product product;

    @ForeignCollectionField()
    private ForeignCollection<StockItem> stockItems;
}
