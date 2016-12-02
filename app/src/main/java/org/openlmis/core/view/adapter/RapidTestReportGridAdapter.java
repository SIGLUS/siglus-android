package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.RapidTestReportGridViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;

import java.util.List;

public class RapidTestReportGridAdapter extends RecyclerView.Adapter<RapidTestReportGridViewHolder> {
    Context context;
    List<RapidTestFormGridViewModel> viewModels;

    public RapidTestReportGridAdapter(List<RapidTestFormGridViewModel> viewModels, Context context) {
        this.viewModels = viewModels;
        this.context = context;
    }

    @Override
    public RapidTestReportGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_rapid_test_report_grid, parent, false);
        parent.getLayoutParams().width = (int) ((((RelativeLayout) parent.getParent().getParent().getParent().getParent()).getWidth()) * 0.8);
        itemView.getLayoutParams().width = (parent.getLayoutParams().width) / getItemCount();
        return new RapidTestReportGridViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RapidTestReportGridViewHolder holder, int position) {
        RapidTestFormGridViewModel viewModel = viewModels.get(position);
        holder.populate(viewModel);
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }
}
