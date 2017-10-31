package org.openlmis.core.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.PatientDataReportFormActivity;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

public class PatientDataReportViewHolderSubmitted extends BaseViewHolder {

    @InjectView(R.id.tv_period)
    TextView tvPeriod;

    @InjectView(R.id.btn_report_entry)
    TextView btnReportEntry;

    private PatientDataReportViewModel viewModel;

    public PatientDataReportViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final PatientDataReportViewModel patientDataReportViewModel) {
        viewModel = patientDataReportViewModel;
        tvPeriod.setText(patientDataReportViewModel.getPeriod().toString());
        if (btnReportEntry != null) {
            btnReportEntry.setOnClickListener(goToPatientDataReportFormActivity());
        }
    }

    public SingleClickButtonListener goToPatientDataReportFormActivity() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                ((BaseActivity) context).loading();
                ((Activity) context).startActivityForResult(PatientDataReportFormActivity
                                .getIntentToMe(context,
                                        PatientDataReportViewModel.DEFAULT_FORM_ID,
                                        viewModel.getPeriod().getBegin()),
                        Constants.REQUEST_CREATE_OR_MODIFY_PATIENT_DATA_REPORT_FORM);
                ((BaseActivity) context).loaded();
            }
        };
    }
}
