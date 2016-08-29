package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@Data
@DatabaseTable(tableName = "lots_on_hand")
public class LotOnHand extends BaseModel {

    @Expose
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Lot lot;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    StockCard stockCard;

    @Expose
    @DatabaseField
    Long quantityOnHand;
}
