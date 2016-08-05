package org.openlmis.core.model.helper;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockMovementItem;

import java.util.List;

public class RnrFormHelper {

    public void initRnrFormItemWithoutMovement(RnrFormItem rnrFormItem, long lastRnrInventory) throws LMISException {
        rnrFormItem.setReceived(0);
        rnrFormItem.setIssued(0);
        rnrFormItem.setAdjustment(0);
        rnrFormItem.setCalculatedOrderQuantity(0L);
        rnrFormItem.setInitialAmount(lastRnrInventory);
        rnrFormItem.setInventory(lastRnrInventory);
    }

    public void assignTotalValues(RnrFormItem rnrFormItem, List<StockMovementItem> stockMovementItems) {
        long totalReceived = 0;
        long totalIssued = 0;
        long totalAdjustment = 0;

        for (StockMovementItem item : stockMovementItems) {
            if (MovementReasonManager.MovementType.RECEIVE == item.getMovementType()) {
                totalReceived += item.getMovementQuantity();
            } else if (MovementReasonManager.MovementType.ISSUE == item.getMovementType()) {
                totalIssued += item.getMovementQuantity();
            } else if (MovementReasonManager.MovementType.NEGATIVE_ADJUST == item.getMovementType()) {
                totalAdjustment -= item.getMovementQuantity();
            } else if (MovementReasonManager.MovementType.POSITIVE_ADJUST == item.getMovementType()) {
                totalAdjustment += item.getMovementQuantity();
            }
        }
        rnrFormItem.setReceived(totalReceived);
        rnrFormItem.setIssued(totalIssued);
        rnrFormItem.setAdjustment(totalAdjustment);

        Long inventory = stockMovementItems.get(stockMovementItems.size() - 1).getStockOnHand();
        rnrFormItem.setInventory(inventory);

        rnrFormItem.setCalculatedOrderQuantity(calculatedOrderQuantity(totalIssued, inventory));
    }

    private long calculatedOrderQuantity(long totalIssued, Long inventory) {
        Long totalRequest = totalIssued * 2 - inventory;
        return totalRequest > 0 ? totalRequest : 0;
    }

}
