package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.ALReportViewHolder;
import org.openlmis.core.view.viewmodel.ALReportItemViewModel;
import org.openlmis.core.view.viewmodel.ALReportViewModel;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;


public class ALReportAdapter extends RecyclerView.Adapter<ALReportViewHolder> {
    private ALReportViewModel alReportViewModel;
    @Setter
    private Boolean editable = true;

    public ALReportAdapter(ALReportViewModel viewModel) {
        alReportViewModel = viewModel;
    }

    @Override
    public ALReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ALReportViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_al_report_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ALReportViewHolder holder, int position) {
        ALReportItemViewModel viewModel = alReportViewModel.itemViewModelList.get(position);
        holder.populate(viewModel);
    }

    @Override
    public int getItemCount() {
        return alReportViewModel.itemViewModelList.size();
    }

    public void refresh(ALReportViewModel viewModel, Boolean editable) {
        alReportViewModel = viewModel;
        this.editable = editable;
        notifyDataSetChanged();
    }
}
