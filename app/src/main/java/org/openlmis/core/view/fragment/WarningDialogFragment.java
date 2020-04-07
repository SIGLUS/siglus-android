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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import lombok.Setter;

public class WarningDialogFragment extends DialogFragment {

    private static final String PARAM_MESSAGE_RES = "messageResId";
    private static final String PARAM_POSITIVE_TEXT_RES = "positiveTextResId";
    private static final String PARAM_NEGATIVE_TEXT_RES = "negativeTextResId";
    private static final String PARAM_STRING_TYPE = "stringType";
    private static final String PARAM_FORMAT_STRING = "typeFormatString";
    private static final String PARAM_INT_STRING = "typeIntString";

    @Setter
    private DialogDelegate delegate;

    public static WarningDialogFragment newInstance(int messageResId, int positiveTextResId, int negativeTextResId) {
        WarningDialogFragment dialog = new WarningDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_MESSAGE_RES, messageResId);
        bundle.putInt(PARAM_POSITIVE_TEXT_RES, positiveTextResId);
        bundle.putInt(PARAM_NEGATIVE_TEXT_RES, negativeTextResId);
        bundle.putString(PARAM_STRING_TYPE, PARAM_INT_STRING);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static WarningDialogFragment newInstanceForDeleteProduct(String messageResId, String positiveTextResId, String negativeTextResId) {
        WarningDialogFragment dialog = new WarningDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_MESSAGE_RES, messageResId);
        bundle.putString(PARAM_POSITIVE_TEXT_RES, positiveTextResId);
        bundle.putString(PARAM_NEGATIVE_TEXT_RES, negativeTextResId);
        bundle.putString(PARAM_STRING_TYPE, PARAM_FORMAT_STRING);
        dialog.setArguments(bundle);
        dialog.setCancelable(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_warning, container, false);
        initUI(contentView);
        return contentView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (isForDeletedProductWarning()) {
            dialog.setCanceledOnTouchOutside(false);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        setDialogAttributes();
    }

    private void initUI(View contentView) {
        TextView tvMessage = (TextView) contentView.findViewById(R.id.dialog_message);
        Button btnNegative = (Button) contentView.findViewById(R.id.btn_cancel);
        Button btnPositive = (Button) contentView.findViewById(R.id.btn_del);

        SingleClickButtonListener singleClickButtonListener = getSingleClickButtonListener();
        btnNegative.setOnClickListener(singleClickButtonListener);
        btnPositive.setOnClickListener(singleClickButtonListener);

        if (isForDeletedProductWarning()) {
            tvMessage.setText(getArguments().getString(PARAM_MESSAGE_RES));
            LinearLayout layout = (LinearLayout) contentView.findViewById(R.id.btn_group);
            layout.setWeightSum(1f);
            btnPositive.setText(getArguments().getString(PARAM_POSITIVE_TEXT_RES));
            btnNegative.setVisibility(View.GONE);
        } else {
            tvMessage.setText(getArguments().getInt(PARAM_MESSAGE_RES));
            btnPositive.setText(getArguments().getInt(PARAM_POSITIVE_TEXT_RES));
            btnNegative.setText(getArguments().getInt(PARAM_NEGATIVE_TEXT_RES));
        }

    }

    private boolean isForDeletedProductWarning() {
        return PARAM_FORMAT_STRING.equals(getArguments().getString(PARAM_STRING_TYPE));
    }


    private void setDialogAttributes() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        Window window = getDialog().getWindow();
        if (window != null) {
            params.copyFrom(getDialog().getWindow().getAttributes());
        }
        params.width = (int) (getDialog().getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        getDialog().getWindow().setAttributes(params);
    }

    public SingleClickButtonListener getSingleClickButtonListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                if (delegate != null && v.getId() == R.id.btn_del) {
                    delegate.onPositiveClick();
                }
                dismiss();
            }
        };
    }

    public interface DialogDelegate {
        void onPositiveClick();
    }
}
