package org.openlmis.core.view.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.fragment.BaseDialogFragment;

import lombok.NonNull;
import roboguice.inject.InjectView;

public class AddLotDialog extends BaseDialogFragment implements View.OnClickListener {

    @InjectView(R.id.dp_add_new_lot)
    DatePicker datePicker;

    @InjectView(R.id.btn_cancel)
    public TextView btnCancel;

    @InjectView(R.id.btn_complete)
    public Button btnComplete;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_lot, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DatePickerDialogWithoutDay.hideDay(datePicker);
        btnCancel.setOnClickListener(this);
        btnComplete.setOnClickListener(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_complete:
                break;
            case R.id.btn_cancel:
                this.dismiss();
                break;
            default:
                break;
        }

    }
}
