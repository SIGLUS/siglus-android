package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.RapidTestReportGridViewHolder;
import org.openlmis.core.view.holder.RapidTestReportObservationRowViewHolder;
import org.openlmis.core.view.holder.RapidTestReportRowViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class RapidTestReportRowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int ITEM_VIEW = 1;
    private final int OBSERVATION_VIEW = 2;
    private List<RapidTestFormItemViewModel> serviceLists;
    private RapidTestReportViewModel rapidTestReportViewModel;

    @Setter
    private Boolean editable = true;
    private RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener;

    public RapidTestReportRowAdapter(RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
        this.quantityChangeListener = quantityChangeListener;
        this.serviceLists = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == serviceLists.size()) {
            return OBSERVATION_VIEW;
        }
        return ITEM_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rapid_test_report_row, parent, false);
            return new RapidTestReportRowViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rapid_test_report_observation_row, parent, false);
            return new RapidTestReportObservationRowViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW:
                RapidTestReportRowViewHolder viewHolder = (RapidTestReportRowViewHolder) holder;
                final RapidTestFormItemViewModel viewModel = serviceLists.get(position);
                viewHolder.setIsRecyclable(false);
                viewHolder.populate(viewModel, editable, quantityChangeListener);
                break;
            case OBSERVATION_VIEW:
                RapidTestReportObservationRowViewHolder observationRowViewHolder = (RapidTestReportObservationRowViewHolder) holder;
                observationRowViewHolder.setIsRecyclable(false);
                observationRowViewHolder.populate(rapidTestReportViewModel);
                break;
            default:
                break;

        }
    }

    @Override
    public int getItemCount() {
        return serviceLists.isEmpty() ? 0 : serviceLists.size() + 1;
    }

    public void refresh(RapidTestReportViewModel viewModel) {
        rapidTestReportViewModel = viewModel;
        serviceLists = rapidTestReportViewModel.getItemViewModelList();
        this.editable = viewModel.isEditable();
        notifyDataSetChanged();
    }

    public void updateTotal() {
        notifyItemChanged(getItemCount() - 2);
    }

    public void updateAPE() {
        notifyItemChanged(getItemCount() - 1);
    }
}
