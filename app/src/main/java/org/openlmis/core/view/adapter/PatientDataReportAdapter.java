package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.PatientDataReportViewHolder;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import java.util.List;

public class PatientDataReportAdapter extends RecyclerView.Adapter<PatientDataReportViewHolder>{
    private Context context;
    private List<PatientDataReportViewModel> viewModels;

    public PatientDataReportAdapter(Context context, List<PatientDataReportViewModel> viewModels) {
        this.context = context;
        this.viewModels = viewModels;
    }

    @Override
    public PatientDataReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PatientDataReportViewHolder(LayoutInflater.from(context).inflate(R.layout.item_patient_data_report, parent, false));
    }

    @Override
    public void onBindViewHolder(PatientDataReportViewHolder holder, int position) {
        holder.populate(viewModels.get(position));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }
}
