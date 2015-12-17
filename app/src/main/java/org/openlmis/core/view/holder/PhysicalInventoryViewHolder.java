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
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.openlmis.core.view.widget.InputFilterMinMax;

import roboguice.inject.InjectView;

public class PhysicalInventoryViewHolder extends BaseViewHolder {

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

    public PhysicalInventoryViewHolder(View itemView) {
        super(itemView);
        etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
        etQuantity.setHint(R.string.hint_quantity_in_stock);
    }


    public void populate(StockCardViewModel stockCardViewModel, String queryKeyWord) {
        final EditTextWatcher textWatcher = new EditTextWatcher(stockCardViewModel);
        etQuantity.removeTextChangedListener(textWatcher);

        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stockCardViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stockCardViewModel.getStyledUnit()));

        tvStockOnHandInInventory.setText(context.getString(R.string.label_physical_inventory_stock_on_hand,
                Long.toString(stockCardViewModel.getStockOnHand())));

        etQuantity.setText(stockCardViewModel.getQuantity());
        etQuantity.post(new Runnable() {
            @Override
            public void run() {
                etQuantity.addTextChangedListener(textWatcher);
            }
        });

        expireDateViewGroup.initExpireDateViewGroup(stockCardViewModel, false);

        if (stockCardViewModel.isValidate()) {
            lyQuantity.setErrorEnabled(false);
        } else {
            lyQuantity.setError(context.getString(R.string.msg_inventory_check_failed));
        }
    }

    class EditTextWatcher extends SingleTextWatcher {

        private final StockCardViewModel viewModel;

        public EditTextWatcher(StockCardViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            viewModel.setHasDataChanged(true);
            viewModel.setQuantity(editable.toString());
        }
    }

}
