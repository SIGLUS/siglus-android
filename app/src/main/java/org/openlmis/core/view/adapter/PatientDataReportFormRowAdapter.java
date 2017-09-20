package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.PatientDataReportRowViewHolder;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import java.util.List;

public class PatientDataReportFormRowAdapter extends RecyclerView.Adapter<PatientDataReportRowViewHolder>{

    private List<PatientDataReportViewModel> viewModels;

    public PatientDataReportFormRowAdapter(List<PatientDataReportViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    @Override
    public PatientDataReportRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_data_report_form_row, parent, false);
        return new PatientDataReportRowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PatientDataReportRowViewHolder holder, int position) {
        final PatientDataReportViewModel viewModel = viewModels.get(position);
        holder.populate(viewModel, true);
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }
}
