package org.openlmis.core.view.holder;

import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.activity.InventoryActivity;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import roboguice.inject.InjectView;

public class LotMovementViewHolder extends BaseViewHolder {

    @InjectView(R.id.stock_on_hand_in_lot)
    private TextView tvStockOnHandInLot;

    @InjectView(R.id.et_lot_amount)
    private EditText etLotAmount;

    @InjectView(R.id.ly_lot_amount)
    private TextInputLayout lyLotAmount;

    @InjectView(R.id.et_lot_info)
    private EditText etLotInfo;

    @InjectView(R.id.vg_soh_lot)
    private LinearLayout lySOHLot;

    public LotMovementViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final LotMovementViewModel viewModel) {
        etLotInfo.setText(viewModel.getLotNumber() + " - " + viewModel.getExpiryDate());
        etLotAmount.setText(viewModel.getQuantity());
        if (viewModel.isValid()) {
            lyLotAmount.setErrorEnabled(false);
        } else {
            lyLotAmount.setError(context.getResources().getString(R.string.msg_inventory_check_failed));
        }
        etLotAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.setQuantity(etLotAmount.getText().toString());
            }
        });

        if (context instanceof InventoryActivity) {
            lySOHLot.setVisibility(View.GONE);
            etLotInfo.setEnabled(false);
            etLotInfo.setTextColor(Color.BLACK);
            etLotInfo.setBackground(null);
        }
    }
}
