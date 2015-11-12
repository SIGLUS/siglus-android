package org.openlmis.core.view.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.openlmis.core.R;

import java.security.Signature;

import lombok.Getter;
import lombok.Setter;

public class SignatureDialog extends DialogFragment implements View.OnClickListener {

    Button btnCancel;
    Button btnSign;

    EditText etSignature;
    TextInputLayout lySignature;

    @Getter
    @Setter
    DialogDelegate delegate;
    private View contentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.dialog_inventory_signature, container, false);

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
        btnSign = (Button) contentView.findViewById(R.id.btn_done);
        etSignature = (EditText) contentView.findViewById(R.id.et_signature);
        lySignature = (TextInputLayout) contentView.findViewById(R.id.ly_signature);

        btnCancel.setOnClickListener(this);
        btnSign.setOnClickListener(this);
    }

    private void setDialogAttributes() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(getDialog().getWindow().getAttributes());
        params.width = (int) (getDialog().getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        getDialog().getWindow().setAttributes(params);
    }

    private boolean checkSignature(String signature) {
        System.out.println(signature);
        return signature.length() >= 2 && signature.matches("[a-zA-Z.]+");
    }

    @Override
    public void onClick(View v) {
        if (delegate == null) {
            return;
        }

        if (v.getId() == R.id.btn_done) {
            String signature = etSignature.getText().toString().trim();
            if (checkSignature(signature)) {
                delegate.onSign(signature);
                dismiss();
            } else {
                lySignature.setError("Signature not valid");
            }
        } else {
            delegate.onCancel();
            dismiss();
        }
    }

    public interface DialogDelegate {
        void onCancel();

        void onSign(String sign);
    }
}
