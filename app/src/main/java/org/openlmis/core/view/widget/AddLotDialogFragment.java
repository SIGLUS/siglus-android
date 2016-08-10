package org.openlmis.core.view.widget;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.view.fragment.BaseDialogFragment;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import java.util.Calendar;
import java.util.GregorianCalendar;

import lombok.Getter;
import lombok.Setter;
import roboguice.inject.InjectView;

public class AddLotDialogFragment extends BaseDialogFragment {

    @InjectView(R.id.et_lot_number)
    EditText etLotNumber;

    @InjectView(R.id.dp_add_new_lot)
    private DatePicker datePicker;

    @InjectView(R.id.btn_cancel)
    private TextView btnCancel;

    @InjectView(R.id.btn_complete)
    private Button btnComplete;

    @InjectView(R.id.ly_lot_number)
    TextInputLayout lyLotNumber;

    @InjectView(R.id.tv_expiry_date_warning)
    TextView expiryDateWarning;

    @Getter
    private LotMovementViewModel viewModel;

    @Setter
    private View.OnClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_lot, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideDay();
        btnCancel.setOnClickListener(listener);
        btnComplete.setOnClickListener(listener);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void hideDay() {
        if (datePicker == null) {
            return;
        }
        ViewGroup datePickerLayout = (ViewGroup) datePicker.getChildAt(0);
        if (datePickerLayout == null) {
            return;
        }

        try {
            int dayIdentifier = Resources.getSystem().getIdentifier("day", "id", "android");
            ViewGroup pickers = (ViewGroup) datePickerLayout.getChildAt(0);
            for (int i = 0; i < pickers.getChildCount(); i++) {
                View childView = pickers.getChildAt(i);
                if (childView.getId() == dayIdentifier) {
                    childView.setVisibility(View.INVISIBLE);
                    return;
                }

            }
        } catch (NullPointerException e) {
            new LMISException(e).reportToFabric();
        }
    }

    public void validate() {
        clearErrorMessage();
        if (etLotNumber.getText().toString().trim().isEmpty()) {
            lyLotNumber.setError(getResources().getString(R.string.msg_empty_lot_number));
            etLotNumber.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
        }

        Calendar today = GregorianCalendar.getInstance();
        GregorianCalendar enteredExpiryDate = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), DatePickerDialogWithoutDay.getLastDayOfMonth(datePicker));
        if (enteredExpiryDate.before(today)) {
            expiryDateWarning.setVisibility(View.VISIBLE);
        }
    }

    private void clearErrorMessage() {
        lyLotNumber.setErrorEnabled(false);
        expiryDateWarning.setVisibility(View.GONE);
    }
}
