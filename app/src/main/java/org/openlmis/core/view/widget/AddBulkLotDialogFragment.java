package org.openlmis.core.view.widget;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.openlmis.core.R;

import lombok.Getter;
import roboguice.inject.InjectView;

public class AddBulkLotDialogFragment extends AddLotDialogFragment {
    public static boolean IS_OCCUPIED = false;

    @InjectView(R.id.et_soh_amount)
    private EditText etSOHAmount;

    @InjectView(R.id.ly_soh_amount)
    private TextInputLayout lySohAmount;
    @Getter
    private String quantity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_bulk_lot, container, false);
    }

    @Override
    public boolean validate() {
        return validateAmount() && super.validate();
    }

    private boolean validateAmount() {
        quantity = etSOHAmount.getText().toString();
        if (quantity.isEmpty()) {
            lySohAmount.setError(getString(R.string.amount_field_cannot_be_empty));
            return Boolean.FALSE;
        } else if (Integer.parseInt(quantity) <= 0) {
            lySohAmount.setError(getString(R.string.amount_cannot_be_less_or_equal_to_zero));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
