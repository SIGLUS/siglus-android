package org.openlmis.core.view.widget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import lombok.Getter;
import roboguice.inject.InjectView;

public class AddBulkLotDialogFragment extends AddLotDialogFragment{
    public static boolean IS_OCCUPIED = false;

    @InjectView(R.id.et_lot_number)
    private EditText etLotNumber;

    @InjectView(R.id.et_soh_amount)
    private EditText etSOHAmount;

    @Getter
    private String quantity;

    private LotMovementViewModel lotMovementViewModel;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_bulk_lot, container, false);
    }

    @Override
    public boolean validate() {
        super.validate();
        quantity = etSOHAmount.getText().toString();
        return true;
    }
}
