package org.openlmis.core.view.holder;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.enums.PatientDataReportType;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.PatientDataReportFormActivity;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

import static org.openlmis.core.R.id.btn_report_entry;

public class PatientDataReportViewHolderSynced extends PatientDataReportViewHolderBase {

    @InjectView(btn_report_entry)
    private TextView btnViewReport;
    private PatientDataReportViewModel viewModel;

    protected PatientDataReportType patientDataReportType;

    public PatientDataReportViewHolderSynced(Context context, ViewGroup parent, PatientDataReportType patientDataReportType) {
        super(LayoutInflater.from(context).inflate(R.layout.item_patient_data_report_synced, parent, false));
        this.patientDataReportType = patientDataReportType;
    }

    public void populate(final PatientDataReportViewModel patientDataReportViewModel) {
        viewModel = patientDataReportViewModel;
        tvPeriod.setText(patientDataReportViewModel.getPeriod().toString());
        tvReportStatus.setText(Html.fromHtml(context.getString(R.string.report_synced_successfully)));
        btnViewReport.setOnClickListener(goToPatientDataReportFormActivity());
    }

    public SingleClickButtonListener goToPatientDataReportFormActivity() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                ((BaseActivity) context).loading();
                ((Activity) context).startActivityForResult(PatientDataReportFormActivity
                                .getIntentToMe(context,
                                        Constants.DEFAULT_FORM_ID,
                                        viewModel.getPeriod().getBegin()),
                        Constants.REQUEST_CREATE_OR_MODIFY_PATIENT_DATA_REPORT_FORM);
                ((BaseActivity) context).loaded();
            }
        };
    }
}
