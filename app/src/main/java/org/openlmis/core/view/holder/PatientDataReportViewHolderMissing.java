package org.openlmis.core.view.holder;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.enums.PatientDataReportType;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.PTVDataReportFormActivity;
import org.openlmis.core.view.activity.PatientDataReportFormActivity;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

import static org.openlmis.core.R.id.btn_create_patient_data_report;


public class PatientDataReportViewHolderMissing extends PatientDataReportViewHolderBase {

    @InjectView(btn_create_patient_data_report)
    private TextView btnReportEntry;

    private PatientDataReportViewModel viewModel;

    protected PatientDataReportType patientDataReportType;

    public PatientDataReportViewHolderMissing(Context context, ViewGroup parent, PatientDataReportType patientDataReportType) {
        super(LayoutInflater.from(context).inflate(R.layout.item_patient_data_report_missing, parent, false));
        this.patientDataReportType = patientDataReportType;
    }

    @Override
    public void populate(PatientDataReportViewModel patientDataReportViewModel) {
        viewModel = patientDataReportViewModel;
        tvPeriod.setText(patientDataReportViewModel.getPeriod().toString());
        if(patientDataReportType.equals(PatientDataReportType.MALARIA)) {
            btnReportEntry.setOnClickListener(goToPatientDataReportFormActivity());
            btnReportEntry.setText(R.string.btn_create_malaria_report);
        }else{
            btnReportEntry.setOnClickListener(goToPTVDataFormActivity());
            btnReportEntry.setText(R.string.create_ptv_data_report);
        }
    }


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

    private SingleClickButtonListener goToPatientDataReportFormActivity() {
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
