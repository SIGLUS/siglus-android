package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

public class LotMovementViewHolder extends BaseViewHolder {

    private final TextView tvStockOnHandInLot;
    private EditText etLotAmount;
    private EditText etLotInfo;

    public LotMovementViewHolder(View itemView) {
        super(itemView);
        etLotAmount = (EditText) itemView.findViewById(R.id.et_lot_amount);
        etLotInfo = (EditText) itemView.findViewById(R.id.et_lot_info);
        tvStockOnHandInLot = (TextView) itemView.findViewById(R.id.stock_on_hand_in_lot);
    }

    public void populate(LotMovementViewModel viewModel) {
        etLotInfo.setText(viewModel.getLotNumber() + " - " + viewModel.getExpiryDate());
        etLotAmount.setText(viewModel.getQuantity());
        tvStockOnHandInLot.setText(viewModel.getLotSoh());
    }
}
