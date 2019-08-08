package org.openlmis.core.view.holder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.adapter.RapidTestReportGridAdapter;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

import roboguice.inject.InjectView;

public class RapidTestReportRowViewHolder extends BaseViewHolder {
    private static final String TAG = RapidTestReportRowViewHolder.class.getSimpleName();

    @InjectView(R.id.rv_rapid_report_grid_item_list)
    RecyclerView rvRapidReportGridListView;

    RapidTestReportGridAdapter adapter;

    public RapidTestReportRowViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(RapidTestFormItemViewModel viewModel, Boolean editable,
                         RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
        setUpGridListView(viewModel, editable, quantityChangeListener);
    }

    public void setUpGridListView(RapidTestFormItemViewModel viewModel, Boolean editable,
                                  RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
        adapter = new RapidTestReportGridAdapter(viewModel.getRapidTestFormGridViewModelList(),
                context,
                !isTotal(viewModel) && editable, isTotal(viewModel) ? null : quantityChangeListener);
        rvRapidReportGridListView.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL,
                false));
        rvRapidReportGridListView.setAdapter(adapter);
    }

    public boolean isTotal(RapidTestFormItemViewModel viewModel) {
        return viewModel.getIssueReason().getDescription().equals(LMISApp.getInstance().getString(R.string.total));
    }

    public boolean isAPEs(RapidTestFormItemViewModel viewModel) {
        return viewModel.getIssueReason().getDescription().equals(LMISApp.getInstance().getString(R.string.ape));
    }
}
