package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import org.openlmis.core.enums.PatientDataReportType;
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.view.holder.PatientDataReportViewHolderBase;
import org.openlmis.core.view.holder.PatientDataReportViewHolderDraft;
import org.openlmis.core.view.holder.PatientDataReportViewHolderMissing;
import org.openlmis.core.view.holder.PatientDataReportViewHolderSubmitted;
import org.openlmis.core.view.holder.PatientDataReportViewHolderSynced;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

import java.util.List;

import lombok.Setter;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportAdapter extends RecyclerView.Adapter<PatientDataReportViewHolderBase> {
    private Context context;
    private PatientDataReportType patientDataReportType;

    @Setter
    private List<PatientDataReportViewModel> viewModels;
    private final SparseArray<Class> viewHolderSparseArray;

    public PatientDataReportAdapter(Context context, PatientDataReportType patientDataReportType) {
        this.context = context;
        this.patientDataReportType = patientDataReportType;
        viewModels = newArrayList();
        viewHolderSparseArray = new SparseArray<>();
        viewHolderSparseArray.put(PatientDataProgramStatus.MISSING.ordinal(), PatientDataReportViewHolderMissing.class);
        viewHolderSparseArray.put(PatientDataProgramStatus.DRAFT.ordinal(), PatientDataReportViewHolderDraft.class);
        viewHolderSparseArray.put(PatientDataProgramStatus.SUBMITTED.ordinal(), PatientDataReportViewHolderSubmitted.class);
        viewHolderSparseArray.put(PatientDataProgramStatus.SYNCED.ordinal(), PatientDataReportViewHolderSynced.class);
    }

    @Override
    public PatientDataReportViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        Class viewHolderClass = viewHolderSparseArray.get(viewType);
        PatientDataReportViewHolderBase viewHolder = null;
        try {
            viewHolder = (PatientDataReportViewHolderBase) viewHolderClass.getConstructor(Context.class, ViewGroup.class, PatientDataReportType.class).newInstance(context, parent, patientDataReportType);
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
    public int getItemViewType(int position) {
        PatientDataReportViewModel viewModel = viewModels.get(position);
        return viewModel.getStatus().ordinal();
    }
}
