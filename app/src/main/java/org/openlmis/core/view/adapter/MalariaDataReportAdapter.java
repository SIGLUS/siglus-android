package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import org.openlmis.core.enums.VIAReportType;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.view.holder.MalariaDataReportViewHolderBase;
import org.openlmis.core.view.holder.MalariaDataReportViewHolderDraft;
import org.openlmis.core.view.holder.MalariaDataReportViewHolderMissing;
import org.openlmis.core.view.holder.MalariaDataReportViewHolderSubmitted;
import org.openlmis.core.view.holder.MalariaDataReportViewHolderSynced;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

import java.util.List;

import lombok.Setter;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class MalariaDataReportAdapter extends RecyclerView.Adapter<MalariaDataReportViewHolderBase> {
    private Context context;
    private VIAReportType VIAReportType;

    @Setter
    private List<PatientDataReportViewModel> viewModels;
    private final SparseArray<Class> viewHolderSparseArray;

    public MalariaDataReportAdapter(Context context, VIAReportType VIAReportType) {
        this.context = context;
        this.VIAReportType = VIAReportType;
        viewModels = newArrayList();
        viewHolderSparseArray = new SparseArray<>();
        viewHolderSparseArray.put(ViaReportStatus.MISSING.ordinal(), MalariaDataReportViewHolderMissing.class);
        viewHolderSparseArray.put(ViaReportStatus.DRAFT.ordinal(), MalariaDataReportViewHolderDraft.class);
        viewHolderSparseArray.put(ViaReportStatus.SUBMITTED.ordinal(), MalariaDataReportViewHolderSubmitted.class);
        viewHolderSparseArray.put(ViaReportStatus.SYNCED.ordinal(), MalariaDataReportViewHolderSynced.class);
    }

    @Override
    public MalariaDataReportViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        Class viewHolderClass = viewHolderSparseArray.get(viewType);
        MalariaDataReportViewHolderBase viewHolder = null;
        try {
            viewHolder = (MalariaDataReportViewHolderBase) viewHolderClass.getConstructor(Context.class, ViewGroup.class, VIAReportType.class).newInstance(context, parent, VIAReportType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MalariaDataReportViewHolderBase holder, int position) {
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
