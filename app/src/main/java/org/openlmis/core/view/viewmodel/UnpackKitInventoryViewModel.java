package org.openlmis.core.view.viewmodel;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.Product;

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
    boolean valid = confirmedNoStockReceived || hasLotChanged() && validateNewLotList();
    if (!valid) {
      shouldShowEmptyLotWarning = true;
    }
    return valid;
  }
}
