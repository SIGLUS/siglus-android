package org.openlmis.core.view.widget;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.presenter.NewStockMovementPresenter;
import org.openlmis.core.view.listener.MovementDateListener;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

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

    private NewStockMovementPresenter newStockMovementPresenter;
    private StockMovementViewModel stockMovementViewModel;
    private MovementReasonManager.MovementType movementType;

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

    public void initMovementDetailsView(NewStockMovementPresenter presenter, MovementReasonManager.MovementType movementType) {
        this.newStockMovementPresenter = presenter;
        this.stockMovementViewModel = presenter.getStockMovementViewModel();
        this.movementType = movementType;
        initView();
    }

    private void initView() {
        if (movementType.equals(MovementReasonManager.MovementType.ISSUE)) {
            lyRequestedQuantity.setVisibility(View.VISIBLE);
        }

        if (MovementReasonManager.MovementType.RECEIVE.equals(movementType)
                || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(movementType)) {
            lyMovementReason.setHint(getResources().getString(R.string.hint_movement_reason_receive));
        }

        setMovementDateClickListener();
    }

    public void setMovementDateClickListener() {
        etMovementDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMovementDate.setEnabled(false);
                showDatePickerDialog(newStockMovementPresenter.getStockCard().getLastStockMovementDate());
            }
        });
        etMovementDate.setKeyListener(null);
    }

    public void setMovementReasonClickListener(OnClickListener movementReasonClickListener) {
        etMovementReason.setOnClickListener(movementReasonClickListener);
        etMovementReason.setKeyListener(null);
    }

    private void showDatePickerDialog(Date previousMovementDate) {
        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(getContext(), DatePickerDialog.BUTTON_NEUTRAL,
                new MovementDateListener(newStockMovementPresenter.getStockMovementViewModel(), previousMovementDate, etMovementDate),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                etMovementDate.setEnabled(true);
            }
        });
        dialog.show();
    }

    public void setMovementReasonEnable(boolean movementReasonEnable) {
        etMovementReason.setEnabled(movementReasonEnable);
    }

    public void setMovementQuantityVisibility(int movementQuantityVisibility) {
        lyMovementQuantity.setVisibility(movementQuantityVisibility);
    }

    public void setMovementModelValue() {
        stockMovementViewModel.setMovementDate(etMovementDate.getText().toString());
        stockMovementViewModel.setDocumentNo(etDocumentNumber.getText().toString());
        stockMovementViewModel.setRequested(etRequestedQuantity.getText().toString());
        HashMap<MovementReasonManager.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(movementType, etMovementQuantity.getText().toString());
        stockMovementViewModel.setTypeQuantityMap(quantityMap);
        stockMovementViewModel.setSignature(etMovementSignature.getText().toString());
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
    }

    public void showMovementReasonEmptyError() {
        lyMovementReason.setError(getResources().getString(R.string.msg_empty_movement_reason));
        etMovementReason.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    public void showMovementQuantityError(String errorMsg) {
        lyMovementQuantity.setError(errorMsg);
        etMovementQuantity.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    public void showSignatureError(String errorMsg) {
        lyMovementSignature.setError(errorMsg);
        etMovementSignature.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    public void setMovementReasonText(String movementReasonText) {
        etMovementReason.setText(movementReasonText);
    }
}
