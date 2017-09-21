package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.presenter.PatientDataReportFormPresenter;
import org.openlmis.core.view.adapter.PatientDataReportFormRowAdapter;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import roboguice.inject.InjectView;

public class PatientDataReportRowViewHolder extends BaseViewHolder {

    public static final int MALARIA_TOTAL_PRODUCTS = 4;
    @InjectView(R.id.tv_title_row)
    public TextView tvTitleRow;

    @InjectView(R.id.et_current_treatment_6x1)
    public EditText etCurrentTreatment6x1;

    @InjectView(R.id.et_current_treatment_6x2)
    public EditText etCurrentTreatment6x2;

    @InjectView(R.id.et_current_treatment_6x3)
    public EditText etCurrentTreatment6x3;

    @InjectView(R.id.et_current_treatment_6x4)
    public EditText etCurrentTreatment6x4;

    @InjectView(R.id.et_existing_stock_6x1)
    public EditText etExistingStock6x1;

    @InjectView(R.id.et_existing_stock_6x2)
    public EditText etExistingStock6x2;

    @InjectView(R.id.et_existing_stock_6x3)
    public EditText etExistingStock6x3;

    @InjectView(R.id.et_existing_stock_6x4)
    public EditText etExistingStock6x4;

    private PatientDataReportViewModel viewModel;

    private PatientDataReportFormPresenter presenter;

    private PatientDataReportFormRowAdapter adapter;

    public PatientDataReportRowViewHolder(View itemView, PatientDataReportFormPresenter presenter, PatientDataReportFormRowAdapter adapter) {
        super(itemView);
        this.presenter = presenter;
        this.adapter = adapter;

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
