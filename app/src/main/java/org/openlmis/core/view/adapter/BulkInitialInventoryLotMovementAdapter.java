package org.openlmis.core.view.adapter;

import java.util.List;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

public class BulkInitialInventoryLotMovementAdapter extends LotMovementAdapter {

  public BulkInitialInventoryLotMovementAdapter(List<LotMovementViewModel> data) {
    super(data);
  }

  public BulkInitialInventoryLotMovementAdapter(List<LotMovementViewModel> data,
      String productName) {
    super(data, productName);
  }

  public int validateLotNonEmptyQuantity() {
    int position = -1;
    for (LotMovementViewModel lotMovementViewModel : lotList) {
      lotMovementViewModel.setValid(true);
      lotMovementViewModel.setQuantityLessThanSoh(true);
    }
    for (int i = 0; i < lotList.size(); i++) {
      if (!lotList.get(i).validateLotWithNoEmptyFields()) {
        position = i;
        break;
      }
    }

    this.notifyDataSetChanged();
    return position;
  }

  public int validateLotPositiveQuantity() {
    int position = -1;
    for (LotMovementViewModel lotMovementViewModel : lotList) {
      lotMovementViewModel.setValid(true);
      lotMovementViewModel.setQuantityLessThanSoh(true);
    }
    for (int i = 0; i < lotList.size(); i++) {
      if (!lotList.get(i).validateLotWithPositiveQuantity()) {
        position = i;
        break;
      }
    }

    this.notifyDataSetChanged();
    return position;
  }
}
