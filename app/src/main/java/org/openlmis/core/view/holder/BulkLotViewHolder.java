package org.openlmis.core.view.holder;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.adapter.BulkLotAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import lombok.Getter;
import roboguice.inject.InjectView;

public class BulkLotViewHolder extends BaseViewHolder {
    public static final String ADD_LOT = "add_new_lot";
    public static final String QUANTITY_ZERO = "0";

    @InjectView(R.id.tv_lot_information)
    TextView tvLotInformation;

    @InjectView(R.id.et_amount)
    EditText etAmount;

    @InjectView(R.id.btn_delete_lot)
    public ImageButton btnDeleteLot;

    @Getter
    private LotMovementViewModel viewModel;
    private InventoryViewModel inventoryViewModel;
    private BulkLotAdapter bulkLotAdapter;

    public BulkLotViewHolder(View itemView, InventoryViewModel inventoryViewModel, BulkLotAdapter bulkLotAdapter) {
        super(itemView);
        this.inventoryViewModel = inventoryViewModel;
        this.bulkLotAdapter = bulkLotAdapter;
        itemView.setTag(getAdapterPosition());
    }

    public void populate(final LotMovementViewModel lotViewModel) {
        this.viewModel = lotViewModel;
        tvLotInformation.setText(lotViewModel.getLotNumber() + " & " + lotViewModel.getExpiryDate());
        etAmount.setText(lotViewModel.getQuantity());
        btnDeleteLot.setOnClickListener(bulkLotAdapter.getRemoveLotListener());
        etAmount.addTextChangedListener(lotQuantityTextWatcher());
    }

    public TextWatcher lotQuantityTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()) {
                    inventoryViewModel.getNewLotMovementViewModelList().get(getAdapterPosition()).setQuantity(QUANTITY_ZERO);
                }else{
                    inventoryViewModel.getNewLotMovementViewModelList().get(getAdapterPosition()).setQuantity(s.toString());
                }
                bulkLotAdapter.updateSOH(inventoryViewModel);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }
}
