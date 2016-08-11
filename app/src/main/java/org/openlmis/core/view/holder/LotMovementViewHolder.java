package org.openlmis.core.view.holder;

import android.graphics.Color;
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

    @InjectView(R.id.et_lot_info)
    private EditText etLotInfo;

    @InjectView(R.id.vg_soh_lot)
    private LinearLayout lySOHLot;

    public LotMovementViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(LotMovementViewModel viewModel) {
        etLotInfo.setText(viewModel.getLotNumber() + " - " + viewModel.getExpiryDate());
        etLotAmount.setText(viewModel.getQuantity());
        tvStockOnHandInLot.setText(viewModel.getLotSoh());

        if (context instanceof InventoryActivity) {
            lySOHLot.setVisibility(View.GONE);
            etLotInfo.setEnabled(false);
            etLotInfo.setTextColor(Color.BLACK);
            etLotInfo.setBackground(null);
        }
    }
}
