package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import org.openlmis.core.model.MalariaProgramStatus;
import org.openlmis.core.view.holder.PatientDataReportViewHolderBase;
import org.openlmis.core.view.holder.PatientDataReportViewHolderPending;
import org.openlmis.core.view.holder.PatientDataReportViewHolderSubmitted;
import org.openlmis.core.view.holder.PatientDataReportViewHolderSynced;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

import java.util.HashMap;
import java.util.List;

import lombok.Setter;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportAdapter extends RecyclerView.Adapter<PatientDataReportViewHolderBase> {
    private Context context;

    @Setter
    private List<PatientDataReportViewModel> viewModels;
    private final HashMap<MalariaProgramStatus, Class> viewHolderMap;

    public PatientDataReportAdapter(Context context) {
        this.context = context;
        viewModels = newArrayList();
        viewHolderMap = new HashMap<>();
        viewHolderMap.put(MalariaProgramStatus.DRAFT, PatientDataReportViewHolderPending.class);
        viewHolderMap.put(MalariaProgramStatus.MISSING, PatientDataReportViewHolderPending.class);
        viewHolderMap.put(MalariaProgramStatus.SUBMITTED, PatientDataReportViewHolderSubmitted.class);
        viewHolderMap.put(MalariaProgramStatus.SYNCED, PatientDataReportViewHolderSynced.class);
    }

    @Override
    public PatientDataReportViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        PatientDataReportViewModel viewModel = viewModels.get(viewType);
        Class viewHolderClass = viewHolderMap.get(viewModel.getStatus());
        PatientDataReportViewHolderBase viewHolder = null;
        try {
            viewHolder = (PatientDataReportViewHolderBase) viewHolderClass.getConstructor(Context.class, ViewGroup.class).newInstance(context, parent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PatientDataReportViewHolderBase holder, int position) {
        holder.populate(viewModels.get(position));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    @Override
    public int getItemViewType(int position) { return position; }
}
