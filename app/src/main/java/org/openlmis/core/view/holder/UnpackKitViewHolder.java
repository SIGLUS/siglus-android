package org.openlmis.core.view.holder;

import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.openlmis.core.view.widget.InputFilterMinMax;

import roboguice.inject.InjectView;

public class UnpackKitViewHolder extends PhysicalInventoryViewHolder {

    @InjectView(R.id.product_name)
    TextView tvProductName;
    @InjectView(R.id.stock_on_hand_in_inventory)
    TextView tvStockOnHandInInventory;
    @InjectView(R.id.product_unit)
    TextView tvProductUnit;
    @InjectView(R.id.tx_quantity)
    EditText etQuantity;
    @InjectView(R.id.ly_quantity)
    TextInputLayout lyQuantity;
    @InjectView(R.id.vg_expire_date_container)
    ExpireDateViewGroup expireDateViewGroup;

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
