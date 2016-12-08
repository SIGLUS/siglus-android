package org.openlmis.core.view.holder;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;

import roboguice.inject.InjectView;

public class RapidTestReportGridViewHolder extends BaseViewHolder {
    @InjectView(R.id.et_consume_rapid_test_report_grid)
    EditText etConsume;

    @InjectView(R.id.et_positive_rapid_test_report_grid)
    EditText etPositive;

    RapidTestFormGridViewModel viewModel;

    public RapidTestReportGridViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(RapidTestFormGridViewModel viewModel, Boolean editable) {
        this.viewModel = viewModel;
        populateData(viewModel);
        setEditable(editable);
        setTextWatcher();
        updateAlert();
    }

    public void setEditable(Boolean editable) {
        etConsume.setFocusableInTouchMode(editable);
        etPositive.setFocusableInTouchMode(editable);
    }

    public void populateData(RapidTestFormGridViewModel viewModel) {
        etConsume.setText(viewModel.getConsumptionValue());
        etPositive.setText(viewModel.getPositiveValue());
    }

    private void setTextWatcher() {
        TextWatcher textWatcherConsume = new TextWatcher(etConsume);
        TextWatcher textWatcherPositive = new TextWatcher(etPositive);
        etPositive.removeTextChangedListener(textWatcherPositive);
        etConsume.removeTextChangedListener(textWatcherConsume);
        etConsume.addTextChangedListener(textWatcherConsume);
        etPositive.addTextChangedListener(textWatcherPositive);
    }

    private void updateAlert() {
        if (viewModel.validate()) {
            etPositive.setTextColor(context.getResources().getColor(R.color.color_black));
            etConsume.setTextColor(context.getResources().getColor(R.color.color_black));
        } else {
            etPositive.setTextColor(context.getResources().getColor(R.color.color_red));
            etConsume.setTextColor(context.getResources().getColor(R.color.color_red));
        }
    }

    class TextWatcher extends SingleTextWatcher {
        private final EditText editText;

        public TextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (editText.getId() == R.id.et_positive_rapid_test_report_grid) {
                viewModel.setPositiveValue(s.toString());
            } else {
                viewModel.setConsumptionValue(s.toString());
            }
            updateAlert();
        }
    }

}
