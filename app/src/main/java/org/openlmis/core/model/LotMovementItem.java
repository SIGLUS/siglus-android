package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.manager.MovementReasonManager;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@DatabaseTable(tableName = "lot_movement_items")
public class LotMovementItem extends BaseModel {

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Lot lot;

    @DatabaseField
    long stockOnHand;

    @Expose
    @SerializedName("quantity")
    @DatabaseField
    long movementQuantity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private StockMovementItem stockMovementItem;

    boolean isResettingSOHFromPhysicalAdjustment = false;

    public long getLotOnHandChange() {
        if (stockMovementItem.getMovementType().equals(MovementReasonManager.MovementType.ISSUE)
                || stockMovementItem.getMovementType().equals(MovementReasonManager.MovementType.NEGATIVE_ADJUST)) {
            return -1 * movementQuantity;
        }
        return movementQuantity;
    }
}
