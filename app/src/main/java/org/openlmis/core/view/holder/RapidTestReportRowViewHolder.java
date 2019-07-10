package org.openlmis.core.view.holder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.adapter.RapidTestReportGridAdapter;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

import roboguice.inject.InjectView;

public class RapidTestReportRowViewHolder extends BaseViewHolder {
    private static final String TAG= RapidTestReportRowViewHolder.class.getSimpleName();
    @InjectView(R.id.tv_row_header)
    TextView tvRowHeader;

    @InjectView(R.id.rv_rapid_report_grid_item_list)
    RapidRecyclerView rvRapidReportGridListView;

    RapidTestReportGridAdapter adapter;

    public RapidTestReportRowViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(RapidTestFormItemViewModel viewModel, Boolean editable, RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
        setUpHeader(viewModel);
        setUpGridListView(viewModel, editable, quantityChangeListener);
    }

    public void setUpGridListView(RapidTestFormItemViewModel viewModel, Boolean editable, RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
        adapter = new RapidTestReportGridAdapter(viewModel.getRapidTestFormGridViewModelList(), context, !isTotal(viewModel) && editable, isTotal(viewModel) ? null : quantityChangeListener);
        rvRapidReportGridListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rvRapidReportGridListView.setAdapter(adapter);
//        rvRapidReportGridListView.stopScroll();
//        rvRapidReportGridListView.setLayoutFrozen(true);
        rvRapidReportGridListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "newState = "+newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "dx = "+dx+",dy="+dy);
            }
        });
    }

    public void setUpHeader(RapidTestFormItemViewModel viewModel) {
        tvRowHeader.setText(viewModel.getIssueReason().getDescription());
        if (isTotal(viewModel) || isAPEs(viewModel)) {
            tvRowHeader.setBackgroundColor(LMISApp.getInstance().getResources().getColor(R.color.color_rapid_test_form_total_header));
        } else {
            tvRowHeader.setBackgroundColor(LMISApp.getInstance().getResources().getColor(R.color.color_rapid_test_form_row_header));
        }
    }

    public boolean isTotal(RapidTestFormItemViewModel viewModel) {
        return viewModel.getIssueReason().getDescription().equals(LMISApp.getInstance().getString(R.string.total));
    }

    public boolean isAPEs(RapidTestFormItemViewModel viewModel) {
        return viewModel.getIssueReason().getDescription().equals(LMISApp.getInstance().getString(R.string.ape));
    }
}
