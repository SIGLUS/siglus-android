package org.openlmis.core.view.widget;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.fragment.BaseDialogFragment;
import org.openlmis.core.view.fragment.SimpleDialogFragment;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import roboguice.inject.InjectView;

public class AddLotDialogFragment extends BaseDialogFragment {

    @InjectView(R.id.ly_lot_number)
    private TextInputLayout lyLotNumber;

    @InjectView(R.id.et_lot_number)
    private EditText etLotNumber;

    @InjectView(R.id.dp_add_new_lot)
    private DatePicker datePicker;

    @InjectView(R.id.btn_cancel)
    private TextView btnCancel;

    @InjectView(R.id.btn_complete)
    private Button btnComplete;

    @InjectView(R.id.tv_expiry_date_warning)
    private TextView expiryDateWarning;

    @InjectView(R.id.drug_name)
    private TextView drugName;

    @Getter
    private String lotNumber;

    @Getter
    private String expiryDate;

    @Setter
    private View.OnClickListener listener;
    private AddLotWithoutNumberListener addLotWithoutNumberListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_lot, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideDay();
        if (getArguments() != null) {
            String drugNameFromArgs = getArguments().getString(Constants.PARAM_STOCK_NAME);
            if (drugNameFromArgs != null) {
                this.drugName.setVisibility(View.VISIBLE);
                this.drugName.setText(drugNameFromArgs);
            }
        }
        btnCancel.setOnClickListener(listener);
        btnComplete.setOnClickListener(listener);
        this.setCancelable(false);
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
                    childView.setVisibility(View.GONE);
                    return;
                }

            }
        } catch (NullPointerException e) {
            new LMISException(e).reportToFabric();
        }
    }

    public boolean validate() {
        clearErrorMessage();
        Date enteredDate = DateUtil.getActualMaximumDate(new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), 1).getTime());
        expiryDate = DateUtil.formatDate(enteredDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);

        if (StringUtils.isBlank(etLotNumber.getText().toString())) {
            if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_empty_lot_number)) {
                showConfirmNoLotNumberDialog();
            } else {
                lyLotNumber.setError(getResources().getString(R.string.msg_empty_lot_number));
                etLotNumber.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
            }
            return false;
        }

        lotNumber = etLotNumber.getText().toString().trim().toUpperCase();
        return true;
    }

    private void showConfirmNoLotNumberDialog() {
        final SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(Html.fromHtml(getString(R.string.title_generate_lot_number)), Html.fromHtml(getString(R.string.msg_confirm_empty_lot_number,drugName.getText())), getString(R.string.btn_confirm_generate_lot_number), getString(R.string.btn_cancel), "on_lot_added");
        dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
            @Override
            public void positiveClick(String tag) {
                dialogFragment.dismiss();
                addLotWithoutNumberListener.addLot(expiryDate);
                AddLotDialogFragment.this.dismiss();
            }

            @Override
            public void negativeClick(String tag) {
                dialogFragment.dismiss();
            }
        });
        dialogFragment.show(getFragmentManager(), "on_lot_added");
    }

    private void clearErrorMessage() {
        lyLotNumber.setErrorEnabled(false);
        expiryDateWarning.setVisibility(View.GONE);
    }

    public boolean hasIdenticalLot(List<String> existingLots) {
        if (existingLots.contains(etLotNumber.getText().toString().toUpperCase())) {
            lyLotNumber.setError(getResources().getString(R.string.error_lot_already_exists));
            etLotNumber.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
            return true;
        }
        return false;
    }

    public void setAddLotWithoutNumberListener(AddLotWithoutNumberListener addLotWithoutNumberListener) {
        this.addLotWithoutNumberListener = addLotWithoutNumberListener;
    }

    public interface AddLotWithoutNumberListener {
        void addLot(String expiryDate);
    }
}
