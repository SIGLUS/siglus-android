package org.openlmis.core.model.helper;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockMovementItem;
import com.google.inject.Inject;

import java.util.List;

public class RnrFormHelper {

    @Inject
    FormHelper formHelper;

    public void initRnrFormItemWithoutMovement(RnrFormItem rnrFormItem, long lastRnrInventory) throws LMISException {
        rnrFormItem.setReceived(0);
        rnrFormItem.setIssued((long) 0);
        rnrFormItem.setAdjustment((long) 0);
        rnrFormItem.setCalculatedOrderQuantity(0L);
        rnrFormItem.setInitialAmount(lastRnrInventory);
        rnrFormItem.setInventory(lastRnrInventory);
    }

    public void assignTotalValues(RnrFormItem rnrFormItem, List<StockMovementItem> stockMovementItems) {
        FormHelper.StockMovementModifiedItem modifiedItem =  formHelper.assignTotalValues(stockMovementItems);
        rnrFormItem.setReceived(modifiedItem.totalReceived);
        rnrFormItem.setIssued(modifiedItem.totalIssued);
        rnrFormItem.setAdjustment(modifiedItem.totalAdjustment);

        Long inventory = stockMovementItems.get(stockMovementItems.size() - 1).getStockOnHand();
        rnrFormItem.setInventory(inventory);

        rnrFormItem.setCalculatedOrderQuantity(calculatedOrderQuantity(modifiedItem.totalIssued, inventory));
    }

    private long calculatedOrderQuantity(long totalIssued, Long inventory) {
        Long totalRequest = totalIssued * 2 - inventory;
        return totalRequest > 0 ? totalRequest : 0;
    }

}
