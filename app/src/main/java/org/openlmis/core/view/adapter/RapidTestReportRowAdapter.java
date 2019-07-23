package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.openlmis.core.R;
import org.openlmis.core.view.fragment.RapidTestReportFormFragment;
import org.openlmis.core.view.holder.RapidTestReportGridViewHolder;
import org.openlmis.core.view.holder.RapidTestReportRowViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class RapidTestReportRowAdapter extends RecyclerView.Adapter<RapidTestReportRowViewHolder> {

    private List<RapidTestFormItemViewModel> viewModels;

    @Setter
    private Boolean editable = true;
    private RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener;

    public RapidTestReportRowAdapter(RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
        this.quantityChangeListener = quantityChangeListener;
        this.viewModels = new ArrayList<>();
    }

    @Override
    public RapidTestReportRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rapid_test_report_row, parent, false);
        return new RapidTestReportRowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RapidTestReportRowViewHolder holder, int position) {
        final RapidTestFormItemViewModel viewModel = viewModels.get(position);
        holder.populate(viewModel, editable, quantityChangeListener);
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void refresh(List<RapidTestFormItemViewModel> itemViewModelList, Boolean editable) {
        viewModels = itemViewModelList;
        this.editable = editable;
        notifyDataSetChanged();
    }

    public void updateTotal() {
        notifyItemChanged(getItemCount() - 2);
    }
    public void updateAPE() {
        notifyItemChanged(getItemCount() - 1);
    }
}
