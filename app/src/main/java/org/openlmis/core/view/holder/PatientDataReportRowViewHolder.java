package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import roboguice.inject.InjectView;

public class PatientDataReportRowViewHolder extends BaseViewHolder  {

    @InjectView(R.id.tv_title_row)
    TextView tvTitleRow;

    @InjectView(R.id.et_current_treatment_6x1)
    EditText etCurrentTreatment6x1;

    @InjectView(R.id.et_current_treatment_6x2)
    EditText etCurrentTreatment6x2;

    @InjectView(R.id.et_current_treatment_6x3)
    EditText etCurrentTreatment6x3;

    @InjectView(R.id.et_current_treatment_6x4)
    EditText etCurrentTreatment6x4;

    @InjectView(R.id.et_existing_stock_6x1)
    EditText etExistingStock6x1;

    @InjectView(R.id.et_existing_stock_6x2)
    EditText etExistingStock6x2;

    @InjectView(R.id.et_existing_stock_6x3)
    EditText etExistingStock6x3;

    @InjectView(R.id.et_existing_stock_6x4)
    EditText etExistingStock6x4;


    public PatientDataReportRowViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(PatientDataReportViewModel viewModel, Boolean editable) {
        tvTitleRow.setText(viewModel.getUsApe());
        etCurrentTreatment6x1.setText(viewModel.getCurrentTreatments().get(0));
        etCurrentTreatment6x2.setText(viewModel.getCurrentTreatments().get(1));
        etCurrentTreatment6x3.setText(viewModel.getCurrentTreatments().get(2));
        etCurrentTreatment6x4.setText(viewModel.getCurrentTreatments().get(3));
        etExistingStock6x1.setText(viewModel.getExistingStock().get(0));
        etExistingStock6x2.setText(viewModel.getExistingStock().get(1));
        etExistingStock6x3.setText(viewModel.getExistingStock().get(2));
        etExistingStock6x4.setText(viewModel.getExistingStock().get(3));
    }
}
