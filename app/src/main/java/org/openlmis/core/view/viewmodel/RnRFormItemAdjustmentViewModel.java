package org.openlmis.core.view.viewmodel;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

import lombok.Data;

@Data
public class RnRFormItemAdjustmentViewModel {
    private long kitStockOnHand;
    private String kitName;
    private int quantity;

    public RnRFormItemAdjustmentViewModel() {
    }

    public RnRFormItemAdjustmentViewModel(long kitStockOnHand, int quantity, String kitName) {
        this.kitStockOnHand = kitStockOnHand;
        this.quantity = quantity;
        this.kitName = kitName;
    }


    public String formatAdjustmentContentForProduct(String productName) {
        return LMISApp.getContext().getResources().getString(R.string.label_adjustment_dialog_adjust_content,
                kitStockOnHand,
                kitName,
                quantity,
                productName
        );
    }

}
