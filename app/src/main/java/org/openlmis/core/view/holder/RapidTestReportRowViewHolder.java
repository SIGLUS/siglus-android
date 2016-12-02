package org.openlmis.core.view.holder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.adapter.RapidTestReportGridAdapter;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

import roboguice.inject.InjectView;

public class RapidTestReportRowViewHolder extends BaseViewHolder {
    @InjectView(R.id.tv_row_header)
    TextView tvRowHeader;

    @InjectView(R.id.rv_rapid_report_grid_item_list)
    RecyclerView rvRapidReportGridListView;

    RapidTestReportGridAdapter adapter;

    public RapidTestReportRowViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(RapidTestFormItemViewModel viewModel) {
        tvRowHeader.setText(viewModel.getIssueReason());
        adapter = new RapidTestReportGridAdapter(viewModel.getRapidTestFormGridViewModelList(), context);
        rvRapidReportGridListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rvRapidReportGridListView.setAdapter(adapter);
    }
}
