package org.openlmis.core.view.holder;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.PTVDataReportFormActivity;
import org.openlmis.core.view.activity.PatientDataReportFormActivity;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

public class PatientDataReportViewHolder extends BaseViewHolder {

    @InjectView(R.id.tv_period)
    TextView tvPeriod;

    @InjectView(R.id.tv_report_status)
    TextView tvReportStatus;

    @InjectView(R.id.btn_create_patient_data_report)
    TextView btnCreatePatientDataReport;

    @InjectView(R.id.btn_create_ptv_data_report)
    TextView btnCreatePtvDataReport;

    private PatientDataReportViewModel viewModel;

    public PatientDataReportViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final PatientDataReportViewModel patientDataReportViewModel) {
        viewModel = patientDataReportViewModel;
        tvReportStatus.setText(Html.fromHtml(context.getString(R.string.msg_patient_data_report_missing)));
        tvPeriod.setText(patientDataReportViewModel.getPeriod().toString());
        btnCreatePatientDataReport.setOnClickListener(goToPatientDataReportFormActivity());
        btnCreatePtvDataReport.setOnClickListener(goToPTVDataFormActivity());
        setGrayHeader();
        setBlueButton(btnCreatePatientDataReport);
        setBlueButton(btnCreatePtvDataReport);
    }

    @NonNull
    private View.OnClickListener goToPTVDataFormActivity() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity) context).loading();
                ((Activity) context).startActivityForResult(PTVDataReportFormActivity.getIntentToMe(context, viewModel.getPeriod().getBegin()), Constants.REQUEST_CREATE_OR_MODIFY_PATIENT_DATA_REPORT_FORM);
                ((BaseActivity) context).loaded();
            }
        };
    }

    public SingleClickButtonListener goToPatientDataReportFormActivity() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                ((BaseActivity) context).loading();
                ((Activity) context).startActivityForResult(PatientDataReportFormActivity.getIntentToMe(context, PatientDataReportViewModel.DEFAULT_FORM_ID, viewModel.getPeriod().getBegin()), Constants.REQUEST_CREATE_OR_MODIFY_PATIENT_DATA_REPORT_FORM);
                ((BaseActivity) context).loaded();
            }
        };
    }

    public void setGrayHeader() {
        tvPeriod.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_description, 0, 0, 0);
        tvPeriod.setBackgroundColor(context.getResources().getColor(R.color.color_draft_title));
        tvPeriod.setTextColor(context.getResources().getColor(R.color.color_white));
    }

    public void setBlueButton(TextView component) {
        component.setBackground(context.getResources().getDrawable(R.drawable.blue_button));
        component.setTextColor(context.getResources().getColor(R.color.color_white));
    }
}
