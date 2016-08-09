package org.openlmis.core.view.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.fragment.BaseDialogFragment;

import roboguice.inject.InjectView;

public class AddLotDialog extends BaseDialogFragment {

    @InjectView(R.id.dp_add_new_lot)
    private DatePicker datePicker;

    @InjectView(R.id.btn_cancel)
    private TextView btnCancel;

    @InjectView(R.id.btn_complete)
    private Button btnComplete;

    private View.OnClickListener listener;

    public AddLotDialog(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_lot, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DatePickerDialogWithoutDay.hideDay(datePicker);
        btnCancel.setOnClickListener(listener);
        btnComplete.setOnClickListener(listener);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
