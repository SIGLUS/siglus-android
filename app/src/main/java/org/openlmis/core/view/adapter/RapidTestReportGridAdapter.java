package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.fragment.RapidTestReportFormFragment;
import org.openlmis.core.view.holder.RapidTestReportGridViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;

import java.util.List;

public class RapidTestReportGridAdapter extends RecyclerView.Adapter<RapidTestReportGridViewHolder> {
    Context context;
    private Boolean editable;
    List<RapidTestFormGridViewModel> viewModels;
    private RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener;

    public RapidTestReportGridAdapter(List<RapidTestFormGridViewModel> viewModels, Context context, Boolean editable, RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
        this.viewModels = viewModels;
        this.context = context;
        this.editable = editable;
        this.quantityChangeListener = quantityChangeListener;
    }

    @Override
    public RapidTestReportGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_rapid_test_report_grid, parent, false);
        itemView.getLayoutParams().width = RapidTestReportFormFragment.GRID_SIZE;
        return new RapidTestReportGridViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RapidTestReportGridViewHolder holder, int position) {
        RapidTestFormGridViewModel viewModel = viewModels.get(position);
        holder.populate(viewModel, editable, quantityChangeListener);
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }
}
