package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.PatientDataReportViewHolder;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

import java.util.List;

import lombok.Setter;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportAdapter extends RecyclerView.Adapter<PatientDataReportViewHolder>{
    private Context context;

    @Setter
    private List<PatientDataReportViewModel> viewModels;

    public PatientDataReportAdapter(Context context) {
        this.context = context;
        viewModels =  newArrayList();
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
