package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.RapidTestReportViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import java.util.List;

public class RapidTestReportAdapter extends RecyclerView.Adapter<RapidTestReportViewHolder> {
    private Context context;
    private List<RapidTestReportViewModel> viewModels;
    public RapidTestReportAdapter(Context context, List<RapidTestReportViewModel> viewModels) {
        this.context = context;
        this.viewModels = viewModels;
    }

    @Override
    public RapidTestReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RapidTestReportViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rapid_test_report,parent,false));
    }

    @Override
    public void onBindViewHolder(RapidTestReportViewHolder holder, int position) {
        holder.populate(viewModels.get(position));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }
}
