package org.openlmis.core.view.holder;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.enums.VIAReportType;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

import roboguice.inject.InjectView;

public class MalariaDataReportViewHolderSubmitted extends MalariaDataReportViewHolderBase {

    @InjectView(R.id.tv_period)
    TextView tvPeriod;

    @InjectView(R.id.btn_report_entry)
    TextView btnReportEntry;

    protected VIAReportType VIAReportType;

    public MalariaDataReportViewHolderSubmitted(Context context, ViewGroup parent, VIAReportType VIAReportType) {
        super(LayoutInflater.from(context).inflate(R.layout.item_patient_data_report_submitted, parent, false));
        this.VIAReportType = VIAReportType;
    }

    public void populate(final PatientDataReportViewModel patientDataReportViewModel) {
        tvPeriod.setText(patientDataReportViewModel.getPeriod().toString());
        tvReportStatus.setText(Html.fromHtml(context.getString(R.string.report_submitted_synced_pending_message)));
    }
}
