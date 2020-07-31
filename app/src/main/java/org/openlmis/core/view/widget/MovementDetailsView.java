package org.openlmis.core.view.widget;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.presenter.NewStockMovementPresenter;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.listener.MovementDateListener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

import static android.graphics.Color.TRANSPARENT;

public class MovementDetailsView extends LinearLayout {
    protected Context context;

    @InjectView(R.id.ly_requested_quantity)
    View lyRequestedQuantity;

    @InjectView(R.id.et_movement_date)
    EditText etMovementDate;

    @InjectView(R.id.ly_movement_date)
    TextInputLayout lyMovementDate;

    @InjectView(R.id.et_document_number)
    EditText etDocumentNumber;

    @InjectView(R.id.et_movement_reason)
    EditText etMovementReason;

    @InjectView(R.id.ly_movement_reason)
    TextInputLayout lyMovementReason;

    @InjectView(R.id.et_requested_quantity)
    EditText etRequestedQuantity;

    @InjectView(R.id.et_movement_quantity)
    EditText etMovementQuantity;

    @InjectView(R.id.ly_movement_quantity)
    TextInputLayout lyMovementQuantity;

    @InjectView(R.id.et_movement_signature)
    EditText etMovementSignature;

    @InjectView(R.id.ly_movement_signature)
    TextInputLayout lyMovementSignature;

    private NewStockMovementPresenter presenter;

    public MovementDetailsView(Context context) {
        super(context);
    }

    public MovementDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflate(context, R.layout.view_movement_details, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public void initMovementDetailsView(NewStockMovementPresenter presenter) {
        this.presenter = presenter;
        initView();
    }

    private void initView() {
        if (presenter.getMovementType().equals(MovementReasonManager.MovementType.ISSUE)) {
            lyRequestedQuantity.setVisibility(View.VISIBLE);
        }

        if (MovementReasonManager.MovementType.RECEIVE.equals(presenter.getMovementType())
                || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(presenter.getMovementType())) {
            lyMovementReason.setHint(getResources().getString(R.string.hint_movement_reason_receive));
        } else {
            lyMovementReason.setHint(getResources().getString(R.string.hint_movement_reason_negative));
        }

        setMovementDateClickListener();
        setSignatureListener();
    }

    private void setSignatureListener() {
        etMovementSignature.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (StringUtils.isEmpty(etMovementSignature.getText())) {
                    showSignatureError(getContext().getString(R.string.msg_empty_signature));
                } else if (etMovementSignature.getText().length() < 2) {
                    showSignatureError(getContext().getString(R.string.hint_signature_error_message));
                } else {
                    lyMovementSignature.setErrorEnabled(false);
                }
            }
        });
        etMovementSignature.setFilters(TextStyleUtil.getSignatureLimitation());
    }

    public void setMovementDateClickListener() {
        etMovementDate.setOnClickListener(view -> {
            etMovementDate.setEnabled(false);
            showDatePickerDialog();
        });
        etMovementDate.setKeyListener(null);
    }

    public void setMovementReasonClickListener(OnClickListener movementReasonClickListener) {
        etMovementReason.setOnClickListener(movementReasonClickListener);
        etMovementReason.setKeyListener(null);
    }

    private void showDatePickerDialog() {
        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Light_Dialog,
                new MovementDateListener(presenter.getViewModel(), presenter.getLastMovementDate(), etMovementDate),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Done", dialog);
        dialog.setOnDismissListener(dialog1 -> etMovementDate.setEnabled(true));
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(TRANSPARENT));
        }
        dialog.show();
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setVisibility(GONE);
    }

    public void setMovementQuantityVisibility(int movementQuantityVisibility) {
        lyMovementQuantity.setVisibility(movementQuantityVisibility);
    }

    public void setMovementModelValue() {
        presenter.getViewModel().setMovementDate(etMovementDate.getText().toString());
        presenter.getViewModel().setDocumentNo(etDocumentNumber.getText().toString());
        presenter.getViewModel().setRequested(etRequestedQuantity.getText().toString());
        HashMap<MovementReasonManager.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(presenter.getMovementType(), etMovementQuantity.getText().toString());
        presenter.getViewModel().setTypeQuantityMap(quantityMap);
        presenter.getViewModel().setSignature(etMovementSignature.getText().toString());
    }

    public void clearTextInputLayoutError() {
        lyMovementDate.setErrorEnabled(false);
        lyMovementReason.setErrorEnabled(false);
        lyMovementQuantity.setErrorEnabled(false);
        lyMovementSignature.setErrorEnabled(false);

    }

    public void showMovementDateEmptyError() {
        lyMovementDate.setError(getResources().getString(R.string.msg_empty_movement_date));
        etMovementDate.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
        requestFocus(etMovementDate);
    }

    public void showMovementReasonEmptyError() {
        lyMovementReason.setError(getResources().getString(R.string.msg_empty_movement_reason));
        etMovementReason.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
        requestFocus(etMovementReason);
    }

    public void requestFocus(final View view) {
        view.post(() -> view.getParent().requestChildFocus(view, view));
    }

    public void showMovementQuantityError(String errorMsg) {
        lyMovementQuantity.setError(errorMsg);
        etMovementQuantity.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
        requestFocus(lyMovementQuantity);
    }

    public void showSignatureError(String errorMsg) {
        lyMovementSignature.setError(errorMsg);
        etMovementSignature.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    public void setMovementReasonText(String movementReasonText) {
        etMovementReason.setText(movementReasonText);
    }

    public boolean validate() {
        clearTextInputLayoutError();
        boolean isValid = validateSignature();
        isValid = validateQuantity() && isValid;
        isValid = validateMovementReason() && isValid;
        isValid = validateMovementDate() && isValid;
        return isValid;
    }

    private boolean validateQuantity() {
        if (!presenter.isKit()) {
            return true;
        }
        if (StringUtils.isEmpty(etMovementQuantity.getText().toString())) {
            showMovementQuantityError(getContext().getString(R.string.msg_empty_quantity));
            return false;
        }
        if (Long.parseLong(etMovementQuantity.getText().toString()) <= 0) {
            showMovementQuantityError(getContext().getString(R.string.msg_entries_error));
            return false;
        }
        if (!presenter.validateKitQuantity()) {
            showMovementQuantityError(getContext().getString(R.string.msg_invalid_quantity));
            return false;
        }
        return true;
    }

    private boolean validateMovementDate() {
        if (StringUtils.isEmpty(etMovementDate.getText().toString())) {
            showMovementDateEmptyError();
            return false;
        }
        return true;
    }

    private boolean validateMovementReason() {
        if (!presenter.getViewModel().validateMovementReason()) {
            showMovementReasonEmptyError();
            return false;
        }
        return true;
    }

    public boolean validateSignature() {
        if (StringUtils.isBlank(etMovementSignature.getText())) {
            showSignatureError(getContext().getString(R.string.msg_empty_signature));
            requestFocus(lyMovementSignature);
            return false;
        } else if (!presenter.getViewModel().validateSignature()) {
            showSignatureError(getContext().getString(R.string.hint_signature_error_message));
            requestFocus(lyMovementSignature);
            return false;
        }
        return true;
    }
}
