package org.openlmis.core.view.holder;

import android.support.annotation.NonNull;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public class PatientDataReportRowViewHolder extends BaseViewHolder {

    public static final String TOTAL = "TOTAL";
    public static final String US = "US";

    public static final int MALARIA_TOTAL_PRODUCTS = 4;
    public static final long EMPTY_VALUE = 0L;

    @InjectView(R.id.tv_title_row)
    private TextView tvTitleRow;

    @InjectView(R.id.et_current_treatment_6x1)
    private EditText etCurrentTreatment6x1;

    @InjectView(R.id.et_current_treatment_6x2)
    private EditText etCurrentTreatment6x2;

    @InjectView(R.id.et_current_treatment_6x3)
    private EditText etCurrentTreatment6x3;

    @InjectView(R.id.et_current_treatment_6x4)
    private EditText etCurrentTreatment6x4;

    @InjectView(R.id.et_existing_stock_6x1)
    private EditText etExistingStock6x1;

    @InjectView(R.id.et_existing_stock_6x2)
    private EditText etExistingStock6x2;

    @InjectView(R.id.et_existing_stock_6x3)
    private EditText etExistingStock6x3;

    @InjectView(R.id.et_existing_stock_6x4)
    private EditText etExistingStock6x4;

    public PatientDataReportRowViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(PatientDataReportViewModel viewModel) {
        EditText[] patientDataCurrentTreatmentsComponents = getDataCurrentTreatmentsEditTexts();
        EditText[] patientDataExistingStockComponents = getExistingStockEditTexts();
        setEditTextValues(viewModel);
        if (viewModel.getType().equals(TOTAL)) {
            setPatientDataComponentsAsNotEditable(patientDataCurrentTreatmentsComponents);
            setPatientDataComponentsAsNotEditable(patientDataExistingStockComponents);
            tvTitleRow.setBackgroundColor(context.getResources().getColor(R.color.patient_data_not_editable));
        }
        if (viewModel.getType().equals(US)) {
            setPatientDataComponentsAsNotEditable(patientDataExistingStockComponents);
        }
    }

    private void setPatientDataComponentsAsNotEditable(EditText[] components) {
        for (int i = 0; i < MALARIA_TOTAL_PRODUCTS; i++) {
            components[i].setFocusable(Boolean.FALSE);
        }
    }

    private void setEditTextValues(PatientDataReportViewModel viewModel) {
        tvTitleRow.setText(viewModel.getType());
        EditText[] patientDataCurrentTreatmentsComponents = getDataCurrentTreatmentsEditTexts();
        EditText[] patientDataExistingStockComponents = getExistingStockEditTexts();
        for (int i = 0; i < MALARIA_TOTAL_PRODUCTS; i++) {
            String currentTreatmentValue = String.valueOf(viewModel.getCurrentTreatments().get(i));
            String existingStockValue = String.valueOf(viewModel.getExistingStock().get(i));
            patientDataCurrentTreatmentsComponents[i].setText(currentTreatmentValue);
            patientDataExistingStockComponents[i].setText(existingStockValue);
        }
    }

    @NonNull
    private EditText[] getExistingStockEditTexts() {
        return new EditText[]{etExistingStock6x1, etExistingStock6x2, etExistingStock6x3, etExistingStock6x4};
    }

    public List<Long> obtainCurrentTreatmentValues() {
        EditText[] patientDataCurrentTreatmentsComponents = getDataCurrentTreatmentsEditTexts();
        List<Long> currentTreatments = obtainPatientDataFieldsValues(patientDataCurrentTreatmentsComponents);
        return currentTreatments;
    }

    @NonNull
    private EditText[] getDataCurrentTreatmentsEditTexts() {
        return new EditText[]{etCurrentTreatment6x1, etCurrentTreatment6x2, etCurrentTreatment6x3, etCurrentTreatment6x4};
    }

    public List<Long> obtainExistingStockValues() {
        EditText[] patientDataExistingStockComponents = getExistingStockEditTexts();
        List<Long> existingStocks = obtainPatientDataFieldsValues(patientDataExistingStockComponents);
        return existingStocks;
    }

    private List<Long> obtainPatientDataFieldsValues(EditText[] components) {
        List<Long> fieldsValues = new ArrayList<>();
        for (int index = 0; index < MALARIA_TOTAL_PRODUCTS; index++) {
            EditText patientDataCurrentTreatmentComponent = components[index];
            String currentValue = patientDataCurrentTreatmentComponent.getText().toString();
            if (!currentValue.isEmpty()) {
                fieldsValues.add(Long.parseLong(currentValue));
            } else {
                fieldsValues.add(EMPTY_VALUE);
            }
        }
        return fieldsValues;
    }

    public void putWatcherInComponents(TextWatcher patientDataTextWatcher) {
        EditText[] components = new EditText[]{etCurrentTreatment6x1, etCurrentTreatment6x2, etCurrentTreatment6x3, etCurrentTreatment6x4, etExistingStock6x1, etExistingStock6x2, etExistingStock6x3, etExistingStock6x4};
        for (int index = 0; index < components.length; index++) {
            components[index].addTextChangedListener(patientDataTextWatcher);
        }
    }
}
