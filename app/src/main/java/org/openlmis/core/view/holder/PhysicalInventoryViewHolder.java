package org.openlmis.core.view.holder;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.openlmis.core.view.widget.InputFilterMinMax;

public class PhysicalInventoryViewHolder extends BaseViewHolder {

    TextView tvProductName;
    TextView tvStockOnHandInInventory;
    TextView tvStockOnHandInInventoryTip;
    TextView tvProductUnit;
    EditText etQuantity;
    TextInputLayout lyQuantity;
    ExpireDateViewGroup expireDateViewGroup;

    public PhysicalInventoryViewHolder(View itemView) {
        super(itemView);
        etQuantity = (EditText) itemView.findViewById(R.id.tx_quantity);
        tvProductName = (TextView) itemView.findViewById(R.id.product_name);
        tvStockOnHandInInventory = (TextView) itemView.findViewById(R.id.stock_on_hand_in_inventory);
        tvStockOnHandInInventoryTip = (TextView) itemView.findViewById(R.id.tv_stock_on_hand_in_inventory_tip);
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

        tvStockOnHandInInventory.setText(Long.toString(inventoryViewModel.getStockOnHand()));

        EditTextWatcher textWatcher = new EditTextWatcher(inventoryViewModel);
        etQuantity.removeTextChangedListener(textWatcher);
        etQuantity.setText(inventoryViewModel.getQuantity());
        etQuantity.addTextChangedListener(textWatcher);

        expireDateViewGroup.initExpireDateViewGroup(inventoryViewModel, false);

        expireDateViewGroup.hideAddExpiryDate(shouldHideExpiryDate(inventoryViewModel));

        if (inventoryViewModel.isValid()) {
            lyQuantity.setErrorEnabled(false);
        } else {
            lyQuantity.setError(context.getString(R.string.msg_inventory_check_failed));
        }

        // This auto populate is added for tester
        if (LMISApp.getInstance().isQAEnabled()) {
            etQuantity.setText(String.valueOf(inventoryViewModel.getStockOnHand()));
        }
    }

    private boolean shouldHideExpiryDate(InventoryViewModel inventoryViewModel) {
        String inventoryQuantity = inventoryViewModel.getQuantity();
        return (inventoryViewModel.getStockOnHand() == 0 && TextUtils.isEmpty(inventoryQuantity))
                || (!TextUtils.isEmpty(inventoryQuantity) && Long.parseLong(inventoryQuantity) == 0);
    }

    class EditTextWatcher extends SingleTextWatcher {

        private final InventoryViewModel viewModel;

        public EditTextWatcher(InventoryViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String quantity = editable.toString();
            afterQuantityChanged(viewModel, quantity);

            expireDateViewGroup.hideAddExpiryDate(shouldHideExpiryDate(viewModel));
        }

    }

    public void afterQuantityChanged(InventoryViewModel viewModel, String quantity) {
        viewModel.setHasDataChanged(true);
        viewModel.setQuantity(quantity);
    }

}
