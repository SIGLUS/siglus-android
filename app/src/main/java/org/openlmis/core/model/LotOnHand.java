package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DatabaseTable(tableName = "lots_on_hand")
@NoArgsConstructor
public class LotOnHand extends BaseModel {

    @Expose
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Lot lot;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    StockCard stockCard;

    @Expose
    @DatabaseField
    Long quantityOnHand;

    public LotOnHand(Lot lot, StockCard stockCard, Long quantityOnHand) {
        this.lot = lot;
        this.stockCard = stockCard;
        this.quantityOnHand = quantityOnHand;
    }
}
