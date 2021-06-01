package org.openlmis.core.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

public class ConfirmGenerateLotNumberDialogFragment extends BaseDialogFragment {

    @InjectView(R.id.tv_generate_lot_number_msg)
    TextView msgGenerateLotNumber;

    @InjectView(R.id.btn_confirm_generate)
    Button btnConfirm;

    @InjectView(R.id.btn_cancel)
    Button btnCancel;

    private SingleClickButtonListener positiveClickListener;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String message = getArguments().getString(Constants.PARAM_MSG_CONFIRM_GENERATE_LOT_NUMBER);
            if (message != null) {
                msgGenerateLotNumber.setText(Html.fromHtml(message));
            }
        }

        btnConfirm.setOnClickListener(positiveClickListener);
        btnCancel.setOnClickListener(v -> ConfirmGenerateLotNumberDialogFragment.this.dismiss());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_confirm_generate_lot_number, container);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void setPositiveClickListener(SingleClickButtonListener listener) {
        positiveClickListener = listener;
    }
}
