package org.openlmis.core.view.holder;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.enums.VIAReportType;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.PTVDataReportFormActivity;
import org.openlmis.core.view.activity.MalariaDataReportFormActivity;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

import static org.openlmis.core.enums.VIAReportType.MALARIA;


public class MalariaDataReportViewHolderDraft extends MalariaDataReportViewHolderBase {

    @InjectView(R.id.btn_create_patient_data_report)
    private TextView btnReportEntry;

    @InjectView(R.id.tv_report_status)
    private TextView tvReportStatus;

    private PatientDataReportViewModel viewModel;

    protected VIAReportType VIAReportType;

    public MalariaDataReportViewHolderDraft(Context context, ViewGroup parent, VIAReportType VIAReportType) {
        super(LayoutInflater.from(context).inflate(R.layout.item_patient_data_report_draft, parent, false));
        this.VIAReportType = VIAReportType;
    }

    @Override
    public void populate(PatientDataReportViewModel patientDataReportViewModel) {
        viewModel = patientDataReportViewModel;
        tvPeriod.setText(patientDataReportViewModel.getPeriod().toString());
        if (VIAReportType.equals(MALARIA)) {
            tvReportStatus.setText(R.string.malaria_report_incomplete);
            btnReportEntry.setText(R.string.continue_editing_malaria);
            btnReportEntry.setOnClickListener(goToPatientDataReportFormActivity());
        } else {
            tvReportStatus.setText(R.string.ptv_report_incomplete);
            btnReportEntry.setText(R.string.continue_editing_ptv);
            btnReportEntry.setOnClickListener(goToPTVDataFormActivity());
        }
    }


    private View.OnClickListener goToPTVDataFormActivity() {
        return v -> {
            ((BaseActivity) context).loading();
            ((Activity) context).startActivityForResult(PTVDataReportFormActivity.getIntentToMe(context, viewModel.getPeriod().getBegin()), Constants.REQUEST_CREATE_OR_MODIFY_PATIENT_DATA_REPORT_FORM);
            ((BaseActivity) context).loaded();
        };
    }

    private SingleClickButtonListener goToPatientDataReportFormActivity() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                ((BaseActivity) context).loading();
                ((Activity) context).startActivityForResult(MalariaDataReportFormActivity
                                .getIntentToMe(context,
                                        Constants.DEFAULT_FORM_ID,
                                        viewModel.getPeriod().getBegin()),
                        Constants.REQUEST_CREATE_OR_MODIFY_PATIENT_DATA_REPORT_FORM);
                ((BaseActivity) context).loaded();
            }
        };
    }
}
