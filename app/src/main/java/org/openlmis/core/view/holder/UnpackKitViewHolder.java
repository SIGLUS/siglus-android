package org.openlmis.core.view.holder;

import android.text.InputFilter;
import android.view.View;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

public class UnpackKitViewHolder extends PhysicalInventoryViewHolder {

    public UnpackKitViewHolder(View itemView) {
        super(itemView);
        etQuantity.setHint(R.string.hint_quantity_in_unpack_kit);
    }

    public void populate(StockCardViewModel stockCardViewModel) {
        etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(0, (int) stockCardViewModel.getStockOnHand())});
        tvStockOnHandInInventory.setText(context.getString(R.string.label_unpack_kit_quantity_expected,
                Long.toString(stockCardViewModel.getStockOnHand())));

        populate(stockCardViewModel, StringUtils.EMPTY);
    }
}
