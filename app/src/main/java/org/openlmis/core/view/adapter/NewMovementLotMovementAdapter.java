package org.openlmis.core.view.adapter;

import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import java.util.List;

public class NewMovementLotMovementAdapter extends LotMovementAdapter {
    public NewMovementLotMovementAdapter(List<LotMovementViewModel> data) {
        super(data);
    }

    public NewMovementLotMovementAdapter(List<LotMovementViewModel> data, String productName) {
        super(data, productName);
    }

    public int validateLotQuantityNotGreaterThanSOH() {
        int position = -1;
        for (LotMovementViewModel lotMovementViewModel : lotList) {
            lotMovementViewModel.setQuantityLessThanSoh(true);
        }
        for (int i = 0; i < lotList.size(); i++) {
            if (!lotList.get(i).validateQuantityNotGreaterThanSOH()) {
                if (position == -1 || position > i) {
                    position = i;
                }
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
                if (position == -1 || position > i) {
                    position = i;
                }
            }
        }

        this.notifyDataSetChanged();
        return position;
    }
}
