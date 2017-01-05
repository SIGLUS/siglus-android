package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.Product;

import lombok.Data;

@Data
public class UnpackKitInventoryViewModel extends InventoryViewModel {
    private boolean shouldShowEmptyLotWarning = false;
    private boolean confirmedNoStockReceived = false;

    public UnpackKitInventoryViewModel(Product product) {
        super(product);
    }

    public boolean shouldShowEmptyLotWarning() {
        return this.shouldShowEmptyLotWarning;
    }

    public boolean hasLotChanged() {
        for (LotMovementViewModel lotMovementViewModel : newLotMovementViewModelList) {
            if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
                return true;
            }
        }
        for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
            if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validate() {
        if (!confirmedNoStockReceived && (!validateLotList() || getLotListQuantityTotalAmount() <= 0)) {
            shouldShowEmptyLotWarning = true;
            return false;
        }
        return true;
    }
}
