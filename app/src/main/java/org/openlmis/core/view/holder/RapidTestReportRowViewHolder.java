package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

import roboguice.inject.InjectView;

public class RapidTestReportRowViewHolder extends BaseViewHolder {
    @InjectView(R.id.tv_row_header)
    TextView tvRowHeader;

    @InjectView(R.id.et_consume_HIV_determine)
    EditText etConsumeHIVDetermine;

    @InjectView(R.id.et_positive_HIV_determine)
    EditText etPositiveHIVDetermine;

    @InjectView(R.id.et_consume_HIV_Unigold)
    EditText etConsumeHIVUnigold;

    @InjectView(R.id.et_positive_HIV_Unigold)
    EditText etPositiveHIVUnigold;

    @InjectView(R.id.et_consume_syphillis)
    EditText etConsumeSyphillis;

    @InjectView(R.id.et_positive_syphillis)
    EditText etPositiveSyphillis;

    @InjectView(R.id.et_consume_malaria)
    EditText etConsumeMalaria;

    @InjectView(R.id.et_positive_malaria)
    EditText etPositiveMalaria;

    public RapidTestReportRowViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(RapidTestFormItemViewModel viewModel) {
        tvRowHeader.setText(viewModel.getIssueReason());
        etConsumeHIVDetermine.setText(viewModel.getConsumeHIVDetermine());
        etPositiveHIVDetermine.setText(viewModel.getPositiveHIVDetermine());
        etConsumeHIVUnigold.setText(viewModel.getConsumeHIVUnigold());
        etPositiveHIVUnigold.setText(viewModel.getPositiveHIVUnigold());
        etConsumeSyphillis.setText(viewModel.getConsumeSyphillis());
        etPositiveSyphillis.setText(viewModel.getPositiveSyphillis());
        etConsumeMalaria.setText(viewModel.getConsumeMalaria());
        etPositiveMalaria.setText(viewModel.getPositiveMalaria());
    }
}
