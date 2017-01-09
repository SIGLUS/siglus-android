package org.openlmis.core.view.widget;

import android.app.Dialog;
import android.app.FragmentManager;
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
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.fragment.BaseDialogFragment;

import lombok.Getter;
import lombok.Setter;
import roboguice.inject.InjectView;

public class SignatureDialog extends BaseDialogFragment {

    @Getter
    @Setter
    DialogDelegate delegate;
    private View contentView;

    @InjectView(R.id.btn_cancel)
    public TextView btnCancel;

    @InjectView(R.id.btn_done)
    public Button btnSign;

    @InjectView(R.id.et_signature)
    public EditText etSignature;

    @InjectView(R.id.ly_signature)
    public TextInputLayout lySignature;

    @InjectView(R.id.tv_signature_title)
    public TextView tvSignatureTitle;

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.dialog_inventory_signature, container, false);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
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
        Bundle arguments = getArguments();
        if (arguments != null) {
            tvSignatureTitle.setText(arguments.getString("title"));
        }

        SingleClickButtonListener singleClickButtonListener = getSingleClickButtonListener();
        btnCancel.setOnClickListener(singleClickButtonListener);
        btnSign.setOnClickListener(singleClickButtonListener);
    }

    private void setDialogAttributes() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(getDialog().getWindow().getAttributes());
        params.width = (int) (getDialog().getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        getDialog().getWindow().setAttributes(params);
    }

    protected boolean checkSignature(String signature) {
        return signature.length() >= 2 && signature.length() <= 5 && signature.matches("\\D+");
    }

    public SingleClickButtonListener getSingleClickButtonListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_done:
                        onDone();
                        break;
                    case R.id.btn_cancel:
                        onCancel();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void onDone() {
        if (!hasDelegate()) {
            return;
        }

        String signature = etSignature.getText().toString().trim();
        if (checkSignature(signature)) {
            delegate.onSign(signature);
            dismiss();
        } else {
            lySignature.setError(getString(R.string.hint_signature_error_message));
        }
    }

    private void onCancel() {
        dismiss();
        if (hasDelegate()) {
            delegate.onCancel();
        }
    }

    private boolean hasDelegate() {
        return delegate != null;
    }

    public static abstract class DialogDelegate {
        public void onCancel() {
        }

        public abstract void onSign(String sign);
    }

    public static Bundle getBundleToMe(String title) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        return bundle;
    }

    public void show(
            FragmentManager manager) {
        super.show(manager, "signature_dialog");
    }

}
