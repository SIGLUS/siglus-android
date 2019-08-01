package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

import roboguice.inject.InjectView;

public class RapidTestReportBodyLeftHeaderViewHolder extends BaseViewHolder {
    private static final String TAG = RapidTestReportBodyLeftHeaderViewHolder.class.getSimpleName();

    @InjectView(R.id.rapid_body_left_title)
    TextView mTitle;

    public RapidTestReportBodyLeftHeaderViewHolder(View itemView) {
        super(itemView);
    }

    public void setUpHeader(RapidTestFormItemViewModel viewModel, int pos) {
        mTitle.setText(viewModel.getIssueReason().getDescription());
        if (isTotal(viewModel) || isAPEs(viewModel)) {
            mTitle.setBackgroundResource(R.drawable.border_top_rapid_test_body_left_ape);
        } else {
            mTitle.setBackgroundResource(R.drawable.border_top_rapid_test_body_left);
        }
    }

    private boolean isTotal(RapidTestFormItemViewModel viewModel) {
        return viewModel.getIssueReason().getDescription().equals(LMISApp.getInstance().getString(R.string.total));
    }

    private boolean isAPEs(RapidTestFormItemViewModel viewModel) {
        return viewModel.getIssueReason().getDescription().equals(LMISApp.getInstance().getString(R.string.ape));
    }
}
