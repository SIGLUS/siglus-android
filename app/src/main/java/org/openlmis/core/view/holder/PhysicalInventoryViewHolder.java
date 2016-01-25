package org.openlmis.core.view.holder;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.openlmis.core.view.widget.InputFilterMinMax;

public class PhysicalInventoryViewHolder extends BaseViewHolder {

    TextView tvProductName;
    TextView tvStockOnHandInInventory;
    TextView tvProductUnit;
    EditText etQuantity;
    TextInputLayout lyQuantity;
    ExpireDateViewGroup expireDateViewGroup;

    public PhysicalInventoryViewHolder(View itemView) {
        super(itemView);
        etQuantity = (EditText) itemView.findViewById(R.id.tx_quantity);
        tvProductName = (TextView) itemView.findViewById(R.id.product_name);
        tvStockOnHandInInventory = (TextView) itemView.findViewById(R.id.stock_on_hand_in_inventory);
        tvProductUnit = (TextView) itemView.findViewById(R.id.product_unit);
        etQuantity = (EditText) itemView.findViewById(R.id.tx_quantity);
        lyQuantity = (TextInputLayout) itemView.findViewById(R.id.ly_quantity);
        expireDateViewGroup = (ExpireDateViewGroup) itemView.findViewById(R.id.vg_expire_date_container);

        etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
        etQuantity.setHint(R.string.hint_quantity_in_stock);
    }

    public void populate(InventoryViewModel inventoryViewModel, String queryKeyWord) {
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));

        tvStockOnHandInInventory.setText(context.getString(R.string.label_physical_inventory_stock_on_hand,
                Long.toString(inventoryViewModel.getStockOnHand())));

        EditTextWatcher textWatcher = new EditTextWatcher(inventoryViewModel);
        etQuantity.removeTextChangedListener(textWatcher);
        etQuantity.setText(inventoryViewModel.getQuantity());
        etQuantity.addTextChangedListener(textWatcher);

        expireDateViewGroup.initExpireDateViewGroup(inventoryViewModel, false);

        if (inventoryViewModel.isValid()) {
            lyQuantity.setErrorEnabled(false);
        } else {
            lyQuantity.setError(context.getString(R.string.msg_inventory_check_failed));
        }
    }

    class EditTextWatcher extends SingleTextWatcher {

        private final InventoryViewModel viewModel;

        public EditTextWatcher(InventoryViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            viewModel.setHasDataChanged(true);
            viewModel.setQuantity(editable.toString());
        }
    }

}
