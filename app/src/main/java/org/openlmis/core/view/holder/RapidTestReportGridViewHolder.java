package org.openlmis.core.view.holder;

import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode;

import roboguice.inject.InjectView;

public class RapidTestReportGridViewHolder extends BaseViewHolder {
    public static final int MAX_INPUT_LENGTH = 9;
    public static final int MAX_TOTAL_LENGTH = 11;
    @InjectView(R.id.et_consume_rapid_test_report_grid)
    EditText etConsume;

    @InjectView(R.id.et_positive_rapid_test_report_grid)
    EditText etPositive;

    @InjectView(R.id.et_unjustified_rapid_test_report_grid)
    EditText etUnjustified;

    @InjectView(R.id.et_warning_border)
    LinearLayout warningLinerLayout;

    RapidTestFormGridViewModel viewModel;
    private Boolean editable;
    private QuantityChangeListener quantityChangeListener;

    public RapidTestReportGridViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(RapidTestFormGridViewModel viewModel, Boolean editable, QuantityChangeListener quantityChangeListener) {
        this.viewModel = viewModel;
        this.editable = editable;
        this.quantityChangeListener = quantityChangeListener;
        populateData(viewModel);
        setEditable(editable);
        updateEditTextMaxLength();
        setTextWatcher();
        updateAlert();
        updateGridViewHaveValueAlert();
    }

    private void updateGridViewHaveValueAlert() {
        if (viewModel.isNeedAddGridViewWarning()){
            warningLinerLayout.setBackground(context.getResources().getDrawable(R.drawable.border_bg_red));
            return;
        }
        warningLinerLayout.setBackground(null);
    }

    private void updateEditTextMaxLength() {
        if (isInTotalRow()) {
            etConsume.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_TOTAL_LENGTH)});
            etPositive.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_TOTAL_LENGTH)});
        } else {
            etConsume.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_INPUT_LENGTH)});
            etPositive.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_INPUT_LENGTH)});
        }
    }

    public void setEditable(Boolean editable) {
        etConsume.setFocusableInTouchMode(editable);
        etPositive.setFocusableInTouchMode(editable);
        etUnjustified.setFocusableInTouchMode(editable);
    }

    public void populateData(RapidTestFormGridViewModel viewModel) {
        etConsume.setText(viewModel.getConsumptionValue());
        etPositive.setText(viewModel.getPositiveValue());
        etUnjustified.setText(viewModel.getUnjustifiedValue());
    }

    private void setTextWatcher() {
        TextWatcher textWatcherConsume = new TextWatcher(etConsume);
        TextWatcher textWatcherPositive = new TextWatcher(etPositive);
        TextWatcher textWatcherUnjustified = new TextWatcher(etUnjustified);
        etPositive.removeTextChangedListener(textWatcherPositive);
        etConsume.removeTextChangedListener(textWatcherConsume);
        etUnjustified.removeTextChangedListener(textWatcherUnjustified);
        if (editable) {
            etConsume.addTextChangedListener(textWatcherConsume);
            etPositive.addTextChangedListener(textWatcherPositive);
            etUnjustified.addTextChangedListener(textWatcherUnjustified);
        }
    }

    private void updateAlert() {
        if (editable && !viewModel.validate() && !viewModel.getIsAPE()) {
            etPositive.setTextColor(context.getResources().getColor(R.color.color_red));
            etConsume.setTextColor(context.getResources().getColor(R.color.color_red));
            etUnjustified.setTextColor(context.getResources().getColor(R.color.color_red));
        } else {
            etPositive.setTextColor(context.getResources().getColor(R.color.color_black));
            etConsume.setTextColor(context.getResources().getColor(R.color.color_black));
            etUnjustified.setTextColor(context.getResources().getColor(R.color.color_black));
        }
    }

    class TextWatcher extends SingleTextWatcher {
        private final EditText editText;

        public TextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void afterTextChanged(Editable s) {
            RapidTestGridColumnCode  gridColumnCode = switchEditIdToGridColumn(editText);
            viewModel.setValue(gridColumnCode, s.toString());
            if (viewModel.getIsAPE()) {
                updateGridViewHaveValueAlert();
                return;
            }
            updateTotal(gridColumnCode);
            updateAlert();
        }

        private RapidTestGridColumnCode switchEditIdToGridColumn(EditText editText) {
            RapidTestGridColumnCode column = RapidTestGridColumnCode.unjustified;
            switch (editText.getId()) {
                case  R.id.et_consume_rapid_test_report_grid:
                    column = RapidTestGridColumnCode.consumption;
                    break;
                case  R.id.et_positive_rapid_test_report_grid:
                    column = RapidTestGridColumnCode.positive;
                    break;
                case  R.id.et_unjustified_rapid_test_report_grid:
                    column = RapidTestGridColumnCode.unjustified;
                    break;
            }
            return  column;
        }

    }

    public void updateTotal(RapidTestGridColumnCode gridColumnCode) {
        if (!isInTotalRow()) {
            quantityChangeListener.updateTotal(viewModel.getColumnCode(), gridColumnCode);
        }
    }

    public boolean isInTotalRow() {
        return quantityChangeListener == null;
    }

    public interface QuantityChangeListener {
        void updateTotal(RapidTestFormGridViewModel.ColumnCode columnCode, RapidTestGridColumnCode gridColumnCode);
    }
}
