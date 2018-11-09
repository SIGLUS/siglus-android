package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.ALReportViewHolder;
import org.openlmis.core.view.viewmodel.ALReportViewModel;

import java.util.ArrayList;
import java.util.List;


public class ALReportAdapter extends RecyclerView.Adapter<ALReportViewHolder> {
    private List<ALReportViewModel> viewModels;

    public ALReportAdapter() {
        viewModels = new ArrayList<>();
    }

    @Override
    public ALReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ALReportViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_al_report_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ALReportViewHolder holder, int position) {
        ALReportViewModel viewModel = viewModels.get(position);
        holder.populate(viewModel);
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }
}
