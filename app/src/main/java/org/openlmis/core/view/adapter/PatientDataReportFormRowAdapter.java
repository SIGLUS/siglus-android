package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.model.Period;
import org.openlmis.core.presenter.PatientDataReportFormPresenter;
import org.openlmis.core.view.holder.PatientDataReportRowViewHolder;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import java.util.List;

public class PatientDataReportFormRowAdapter extends RecyclerView.Adapter<PatientDataReportRowViewHolder> {

    public static final int ROW_US_POSITION = 0;
    public static final int ROW_APE_POSITION = 1;
    private boolean onBind;

    Period period;
    private PatientDataReportFormPresenter presenter;
    private List<PatientDataReportViewModel> viewModels;

    public PatientDataReportFormRowAdapter(List<PatientDataReportViewModel> viewModels, PatientDataReportFormPresenter presenter, Period period) {
        this.period = period;
        this.presenter = presenter;
        this.viewModels = viewModels;
    }

    @Override
    public PatientDataReportRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_data_report_form_row, parent, false);
        return new PatientDataReportRowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PatientDataReportRowViewHolder holder, int position) {
        setOnBind(Boolean.TRUE);
        final PatientDataReportViewModel viewModel = viewModels.get(position);
        holder.populate(viewModel);
        holder.putWatcherInComponents(patientDataTextWatcher(holder, position));
        setOnBind(Boolean.FALSE);
    }

    private void setOnBind(boolean onBind) {
        this.onBind = onBind;
    }

    public TextWatcher patientDataTextWatcher(final PatientDataReportRowViewHolder holder, final int position) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Long> currentTreatments = holder.obtainCurrentTreatmentValues();
                List<Long> existingStocks = holder.obtainExistingStockValues();
                setCurrentTreatmentAndExistingStockInActualRow(currentTreatments, existingStocks, position);
                presenter.generateViewModelsBySpecificPeriod(period);
                viewModels = presenter.getViewModels(period);
                if(!onBind) {
                    int totalRowPosition = viewModels.size() - 1;
                    notifyItemChanged(totalRowPosition);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private void setCurrentTreatmentAndExistingStockInActualRow(List<Long> currentTreatments, List<Long> existingStocks, int position) {
        if (position == ROW_US_POSITION) {
            presenter.setCurrentTreatmentsUs(currentTreatments);
            presenter.setExistingStockUs(existingStocks);
        }
        if (position == ROW_APE_POSITION) {
            presenter.setCurrentTreatmentsApe(currentTreatments);
            presenter.setExistingStockApe(existingStocks);
        }
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }


}
