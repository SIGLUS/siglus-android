package org.openlmis.core.view.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import org.openlmis.core.R;

import lombok.Getter;
import lombok.Setter;

public class WarningDialog extends DialogFragment implements View.OnClickListener {
    Button btnCancel;
    Button btnDel;


    @Getter
    @Setter
    DialogDelegate delegate;
    private View contentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.dialog_warning, container, false);

        initUI();
        return contentView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        setDialogAttributes();
    }

    private void initUI() {
        btnCancel = (Button) contentView.findViewById(R.id.btn_cancel);
        btnDel = (Button) contentView.findViewById(R.id.btn_del);

        btnCancel.setOnClickListener(this);
        btnDel.setOnClickListener(this);
    }

    private void setDialogAttributes() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(getDialog().getWindow().getAttributes());
        params.width = (int) (getDialog().getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        getDialog().getWindow().setAttributes(params);
    }


    @Override
    public void onClick(View v) {
        if (delegate == null) {
            dismiss();
            return;
        }
        if (v.getId() == R.id.btn_del) {
            delegate.onDelete();
        }
        dismiss();
    }

    public interface DialogDelegate {

        void onDelete();
    }
}
