package org.openlmis.core.view.holder;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
        Log.e(TAG, "caopeng setUpHeader =" + viewModel.getIssueReason().getDescription());
        if (pos != 0) {
            mTitle.setText(viewModel.getIssueReason().getDescription());
        } else {

        }
    }
}
