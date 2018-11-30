package org.openlmis.core.view.holder;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.model.Period;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.TrackRnREventUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.PhysicalInventoryActivity;
import org.openlmis.core.view.activity.RapidTestReportFormActivity;
import org.openlmis.core.view.activity.SelectPeriodActivity;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class RapidTestReportViewHolder extends BaseViewHolder {
    @InjectView(R.id.tv_period)
    TextView tvPeriod;

    @InjectView(R.id.tv_report_status)
    TextView tvReportStatus;

    @InjectView(R.id.btn_create_patient_data_report)
    TextView btnReportEntry;
    private RapidTestReportViewModel viewModel;

    public RapidTestReportViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final RapidTestReportViewModel rapidTestReportViewModel) {
        Period period = rapidTestReportViewModel.getPeriod();
        if (period != null) {
            String periodStr = LMISApp.getContext().getString(R.string.label_period_date, DateUtil.formatDateWithoutDay(period.getBegin().toDate()), DateUtil.formatDateWithoutDay(period.getEnd().toDate()));
            tvPeriod.setText(periodStr);
        }
        viewModel = rapidTestReportViewModel;
        switch (viewModel.getStatus()) {
            case MISSING:
                String finishPreviousForm;
                finishPreviousForm = context.getString(R.string.label_previous_period_missing);
                setGrayHeader();
                btnReportEntry.setVisibility(View.INVISIBLE);
                tvReportStatus.setText(Html.fromHtml(finishPreviousForm));
                break;
            case CANNOT_DO_MONTHLY_INVENTORY:
                finishPreviousForm = context.getString(R.string.label_training_can_not_create_rapid_Test_rnr);
                setGrayHeader();
                btnReportEntry.setVisibility(View.INVISIBLE);
                tvReportStatus.setText(Html.fromHtml(finishPreviousForm));
                break;
            case FIRST_MISSING:
                tvReportStatus.setText(Html.fromHtml(context.getString(R.string.Rapid_Test_missed_period)));
                setGrayHeader();
                setBlueButton();
                btnReportEntry.setText(context.getString(R.string.btn_select_close_period));
                btnReportEntry.setOnClickListener(goToSelectPeriod());
                break;
            case UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD:
                tvReportStatus.setText(Html.fromHtml(context.getString(R.string.label_uncompleted_rapid_physical_inventory_message)));
                setGrayHeader();
                setBlueButton();
                btnReportEntry.setText(context.getString(R.string.btn_view_uncompleted_physical_inventory));
                btnReportEntry.setOnClickListener(goToInventory());
                break;
            case COMPLETE_INVENTORY:
                tvReportStatus.setText(Html.fromHtml(context.getString(R.string.label_completed_physical_inventory_message, "Rapid Test")));
                setGrayHeader();
                setBlueButton();
                btnReportEntry.setText(context.getString(R.string.btn_view_completed_Rapid_Test_inventory));
                btnReportEntry.setOnClickListener(goToSelectPeriod());
                break;
            case INACTIVE:
                tvReportStatus.setText(Html.fromHtml(context.getString(R.string.inactive_status)));
                tvPeriod.setText(context.getString(R.string.inactive));
                setWhiteHeader();
                setBlueButton();
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
        btnReportEntry.setPadding(60,5,60,0);
        btnReportEntry.setTextColor(context.getResources().getColor(R.color.color_white));
    }

    public void setWhiteButton() {
        btnReportEntry.setBackgroundColor(context.getResources().getColor(R.color.color_white));
        btnReportEntry.setTextColor(context.getResources().getColor(R.color.color_accent));
    }

    @NonNull
    public SingleClickButtonListener goToSelectPeriod() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                ((BaseActivity) context).loading();
                ((Activity) context).startActivityForResult(SelectPeriodActivity.getIntentToMe((Activity) context, Constants.RAPID_TEST_CODE, viewModel.getPeriod()), Constants.REQUEST_SELECT_PERIOD_END);
                TrackRnREventUtil.trackRnRListEvent(TrackerActions.CreateRnR, Constants.RAPID_TEST_CODE);
                ((BaseActivity) context).loaded();
            }
        };
    }

    @NonNull
    public SingleClickButtonListener goToInventory() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                ((BaseActivity) context).loading();
                ((Activity) context).startActivityForResult(PhysicalInventoryActivity.getIntentToMe(context), Constants.REQUEST_FROM_RNR_LIST_PAGE);
                TrackRnREventUtil.trackRnRListEvent(TrackerActions.CreateRnR, Constants.RAPID_TEST_CODE);
                ((BaseActivity) context).loaded();
            }
        };
    }

    @NonNull
    public SingleClickButtonListener goToRapidTestReportActivityListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                ((BaseActivity) context).loading();
                if (viewModel.getRapidTestForm() == null) {
                    ((Activity) context).startActivityForResult(RapidTestReportFormActivity.getIntentToMe(context, RapidTestReportViewModel.DEFAULT_FORM_ID, viewModel.getPeriod(),viewModel.getPeriod().getBegin()), Constants.REQUEST_CREATE_OR_MODIFY_RAPID_TEST_FORM);
                } else {
                    ((Activity) context).startActivityForResult(RapidTestReportFormActivity.getIntentToMe(context, viewModel.getRapidTestForm().getId(), viewModel.getPeriod(), viewModel.getPeriod().getBegin()), Constants.REQUEST_CREATE_OR_MODIFY_RAPID_TEST_FORM);
                }
                ((BaseActivity) context).loaded();
            }
        };
    }
}
