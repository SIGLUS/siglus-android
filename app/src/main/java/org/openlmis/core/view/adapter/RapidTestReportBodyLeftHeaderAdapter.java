package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.RapidTestReportBodyLeftHeaderViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

import java.util.ArrayList;
import java.util.List;

public class RapidTestReportBodyLeftHeaderAdapter extends RecyclerView.Adapter<RapidTestReportBodyLeftHeaderViewHolder> {
    private List<RapidTestFormItemViewModel> viewModels;

    public RapidTestReportBodyLeftHeaderAdapter() {
        this.viewModels = new ArrayList<>();
    }

    @Override
    public RapidTestReportBodyLeftHeaderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rapid_test_report_body_left, parent, false);
        return new RapidTestReportBodyLeftHeaderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RapidTestReportBodyLeftHeaderViewHolder holder, int position) {
        if (position == viewModels.size()) {
            holder.setUpObservationLeftHeaderViewHolder();
            return;
        }
        final RapidTestFormItemViewModel viewModel = viewModels.get(position);
        holder.setUpHeader(viewModel);
    }

    @Override
    public int getItemCount() {
        return viewModels.size() + 1;
    }

    public void refresh(List<RapidTestFormItemViewModel> itemViewModelList) {
        viewModels = itemViewModelList;
        notifyDataSetChanged();
    }
}
