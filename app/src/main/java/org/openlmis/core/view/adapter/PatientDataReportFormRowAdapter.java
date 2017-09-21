package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.openlmis.core.R;
import org.openlmis.core.presenter.PatientDataReportFormPresenter;
import org.openlmis.core.view.holder.PatientDataReportRowViewHolder;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import static org.openlmis.core.view.holder.PatientDataReportRowViewHolder.MALARIA_TOTAL_PRODUCTS;

public class PatientDataReportFormRowAdapter extends RecyclerView.Adapter<PatientDataReportRowViewHolder> {

    private boolean onBind;

    private PatientDataReportFormPresenter presenter;
    private List<PatientDataReportViewModel> viewModels;
    @Getter
    @Setter
    private boolean isNeededToUpdate;

    public PatientDataReportFormRowAdapter(List<PatientDataReportViewModel> viewModels, PatientDataReportFormPresenter presenter) {
        this.viewModels = viewModels;
        this.presenter = presenter;
    }

    @Override
    public PatientDataReportRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_data_report_form_row, parent, false);
        return new PatientDataReportRowViewHolder(itemView, presenter, this);
    }

    @Override
    public void onBindViewHolder(PatientDataReportRowViewHolder holder, int position) {
        onBind = true;
        final PatientDataReportViewModel viewModel = viewModels.get(position);
        holder.populate(viewModel, true);
        holder.etCurrentTreatment6x1.addTextChangedListener(patientDataTextWatcher(holder, position));
        holder.etCurrentTreatment6x2.addTextChangedListener(patientDataTextWatcher(holder, position));
        holder.etCurrentTreatment6x3.addTextChangedListener(patientDataTextWatcher(holder, position));
        holder.etCurrentTreatment6x4.addTextChangedListener(patientDataTextWatcher(holder, position));
        holder.etExistingStock6x1.addTextChangedListener(patientDataTextWatcher(holder, position));
        holder.etExistingStock6x2.addTextChangedListener(patientDataTextWatcher(holder, position));
        holder.etExistingStock6x3.addTextChangedListener(patientDataTextWatcher(holder, position));
        holder.etExistingStock6x4.addTextChangedListener(patientDataTextWatcher(holder, position));
        onBind = false;
    }

    public TextWatcher patientDataTextWatcher(final PatientDataReportRowViewHolder holder, final int position) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> currentTreatments = new ArrayList<>();
                List<String> existingStocks = new ArrayList<>();
                EditText[] patientDataCurrentTreatmentsComponents = new EditText[]{holder.etCurrentTreatment6x1, holder.etCurrentTreatment6x2, holder.etCurrentTreatment6x3, holder.etCurrentTreatment6x4};
                EditText[] patientDataExistingStockComponents = new EditText[]{holder.etExistingStock6x1, holder.etExistingStock6x2, holder.etExistingStock6x3, holder.etExistingStock6x4};
                for (int index = 0; index < MALARIA_TOTAL_PRODUCTS; index++) {
                    EditText patientDataCurrentTreatmentComponent = patientDataCurrentTreatmentsComponents[index];
                    EditText patientDataExistingStockComponent = patientDataExistingStockComponents[index];
                    String currentTreatmentValue = patientDataCurrentTreatmentComponent.getText().toString();
                    String existingStockValue = patientDataExistingStockComponent.getText().toString();
                    if (!currentTreatmentValue.isEmpty()) {
                        currentTreatments.add(currentTreatmentValue);
                    } else {
                        currentTreatments.add("0");
                    }if (!existingStockValue.isEmpty()) {
                        existingStocks.add(existingStockValue);
                    } else {
                        existingStocks.add("0");
                    }
                }

                if (position == 0) {
                    presenter.setCurrentTreatmentsUs(currentTreatments);
                    presenter.setExistingStockUs(existingStocks);
                }
                if (position == 1) {
                    presenter.setCurrentTreatmentsApe(currentTreatments);
                    presenter.setExistingStockApe(existingStocks);
                }

                presenter.generateViewModelsForAvailablePeriods();
                viewModels = presenter.getViewModels();
                if(!onBind) {
                    notifyItemChanged(presenter.getViewModels().size() - 1);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }


    @Override
    public int getItemCount() {
        return viewModels.size();
    }


}
