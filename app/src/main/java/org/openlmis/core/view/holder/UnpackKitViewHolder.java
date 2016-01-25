package org.openlmis.core.view.holder;

import android.text.InputFilter;
import android.view.View;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

public class UnpackKitViewHolder extends PhysicalInventoryViewHolder {

    public UnpackKitViewHolder(View itemView) {
        super(itemView);
        etQuantity.setHint(R.string.hint_quantity_in_unpack_kit);
    }

    public void populate(InventoryViewModel inventoryViewModel) {
        etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(0, (int) inventoryViewModel.getKitExpectQuantity())});

        populate(inventoryViewModel, StringUtils.EMPTY);

        tvStockOnHandInInventory.setText(context.getString(R.string.label_unpack_kit_quantity_expected,
                Long.toString(inventoryViewModel.getKitExpectQuantity())));

    }
}
