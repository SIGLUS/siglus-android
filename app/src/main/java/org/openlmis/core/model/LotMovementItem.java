package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "lot_movement_items")
public class LotMovementItem extends BaseModel{

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Lot lot;

    @DatabaseField
    long stockOnHand;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private StockMovementItem stockMovementItem;
}
