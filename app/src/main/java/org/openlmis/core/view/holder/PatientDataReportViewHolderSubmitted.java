package org.openlmis.core.view.holder;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.enums.PatientDataReportType;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

import roboguice.inject.InjectView;

public class PatientDataReportViewHolderSubmitted extends PatientDataReportViewHolderBase {

    @InjectView(R.id.tv_period)
    TextView tvPeriod;

    @InjectView(R.id.btn_report_entry)
    TextView btnReportEntry;

    protected PatientDataReportType patientDataReportType;

    public PatientDataReportViewHolderSubmitted(Context context, ViewGroup parent, PatientDataReportType patientDataReportType) {
        super(LayoutInflater.from(context).inflate(R.layout.item_patient_data_report_submitted, parent, false));
        this.patientDataReportType = patientDataReportType;
    }

    public void populate(final PatientDataReportViewModel patientDataReportViewModel) {
        tvPeriod.setText(patientDataReportViewModel.getPeriod().toString());
        tvReportStatus.setText(Html.fromHtml(context.getString(R.string.report_submitted_synced_pending_message)));
    }
}
