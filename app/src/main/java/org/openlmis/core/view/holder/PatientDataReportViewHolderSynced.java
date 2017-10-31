package org.openlmis.core.view.holder;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

public class PatientDataReportViewHolderSynced extends PatientDataReportViewHolderBase {

    public PatientDataReportViewHolderSynced(Context context, ViewGroup parent) {
        super(LayoutInflater.from(context).inflate(R.layout.item_patient_data_report_synced, parent, false));
    }

    public void populate(final PatientDataReportViewModel patientDataReportViewModel) {
        tvPeriod.setText(patientDataReportViewModel.getPeriod().toString());
        tvReportStatus.setText(Html.fromHtml(context.getString(R.string.report_synced_successfully)));
    }
}
