package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.EditText;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;

import roboguice.inject.InjectView;

public class RapidTestReportGridViewHolder extends BaseViewHolder {
    @InjectView(R.id.et_consume_rapid_test_report_grid)
    EditText etConsume;

    @InjectView(R.id.et_positive_rapid_test_report_grid)
    EditText etPositive;

    public RapidTestReportGridViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(RapidTestFormGridViewModel viewModel) {
        etConsume.setText(viewModel.getConsumptionValue());
        etPositive.setText(viewModel.getPositiveValue());

        updateAlert(viewModel);
    }

    private void updateAlert(RapidTestFormGridViewModel viewModel) {
        if (viewModel.validate()) {
            etPositive.setTextColor(context.getResources().getColor(R.color.color_black));
        } else {
            etPositive.setTextColor(context.getResources().getColor(R.color.color_red));
        }
    }
}
