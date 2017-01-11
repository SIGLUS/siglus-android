package org.openlmis.core.view.holder;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.RapidTestReportFormActivity;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

public class RapidTestReportViewHolder extends BaseViewHolder {
    @InjectView(R.id.tv_period)
    TextView tvPeriod;

    @InjectView(R.id.tv_report_status)
    TextView tvReportStatus;

    @InjectView(R.id.btn_report_entry)
    TextView btnReportEntry;
    private RapidTestReportViewModel viewModel;

    public RapidTestReportViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final RapidTestReportViewModel rapidTestReportViewModel) {
        tvPeriod.setText(rapidTestReportViewModel.getPeriod().toString());
        viewModel = rapidTestReportViewModel;
        switch (viewModel.getStatus()) {
            case MISSING:
                tvReportStatus.setText(Html.fromHtml(context.getString(R.string.msg_report_missing)));
                setGrayHeader();
                setBlueButton();
                btnReportEntry.setText(context.getString(R.string.btn_create_rapid_test_period));
                btnReportEntry.setOnClickListener(goToRapidTestReportActivityListener());
                break;
            case INCOMPLETE:
                setGrayHeader();
                tvReportStatus.setText(Html.fromHtml(context.getString(R.string.label_incomplete_requisition, context.getString(R.string.title_rapid_test_reports))));
                setWhiteButton();
                btnReportEntry.setText(context.getString(R.string.btn_view_incomplete_requisition, context.getString(R.string.title_rapid_test_reports)));
                btnReportEntry.setOnClickListener(goToRapidTestReportActivityListener());
                break;
            case COMPLETED:
                String error;
                error = context.getString(R.string.label_unsynced_requisition, context.getString(R.string.title_rapid_test_reports));
                setRedHeader();
                tvReportStatus.setText(Html.fromHtml(error));
                break;
            case SYNCED:
                setWhiteHeader();
                tvReportStatus.setText(Html.fromHtml(context.getString(R.string.label_submitted_message, context.getString(R.string.title_rapid_test_reports), DateUtil.formatDate(rapidTestReportViewModel.getSyncedTime()))));
                setWhiteButton();
                btnReportEntry.setText(context.getString(R.string.btn_view_requisition, context.getString(R.string.title_rapid_test_reports)));
                btnReportEntry.setOnClickListener(goToRapidTestReportActivityListener());
                break;
            default:
                break;
        }
    }

    public void setWhiteHeader() {
        tvPeriod.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_green, 0, 0, 0);
        tvPeriod.setBackground(context.getResources().getDrawable(R.drawable.border_bottom));
        tvPeriod.setTextColor(context.getResources().getColor(R.color.color_black));
    }

    public void setRedHeader() {
        tvPeriod.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0);
        tvPeriod.setBackgroundResource(R.color.color_red);
        tvPeriod.setTextColor(context.getResources().getColor(R.color.color_white));
    }

    public void setGrayHeader() {
        tvPeriod.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_description, 0, 0, 0);
        tvPeriod.setBackgroundColor(context.getResources().getColor(R.color.color_draft_title));
        tvPeriod.setTextColor(context.getResources().getColor(R.color.color_white));
    }

    public void setBlueButton() {
        btnReportEntry.setBackground(context.getResources().getDrawable(R.drawable.blue_button));
        btnReportEntry.setTextColor(context.getResources().getColor(R.color.color_white));
    }

    public void setWhiteButton() {
        btnReportEntry.setBackgroundColor(context.getResources().getColor(R.color.color_white));
        btnReportEntry.setTextColor(context.getResources().getColor(R.color.color_accent));
    }

    @NonNull
    public SingleClickButtonListener goToRapidTestReportActivityListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                ((BaseActivity) context).loading();
                if (viewModel.getRapidTestForm() == null) {
                    ((Activity) context).startActivityForResult(RapidTestReportFormActivity.getIntentToMe(context, RapidTestReportViewModel.DEFAULT_FORM_ID, viewModel.getPeriod().getBegin()), Constants.REQUEST_CREATE_OR_MODIFY_RAPID_TEST_FORM);
                } else {
                    ((Activity) context).startActivityForResult(RapidTestReportFormActivity.getIntentToMe(context, viewModel.getRapidTestForm().getId(), viewModel.getPeriod().getBegin()), Constants.REQUEST_CREATE_OR_MODIFY_RAPID_TEST_FORM);
                }
                ((BaseActivity) context).loaded();
            }
        };
    }
}
