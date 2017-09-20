package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.PatientDataAdapter;
import org.openlmis.core.view.presenter.PatientDataReportPresenter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_patient_data_report)
public class PatientDataReportActivity extends BaseReportListActivity {

    @InjectView(R.id.rv_patient_data_periods)
    private RecyclerView rvPatientDataPeriods;

    @InjectPresenter(PatientDataReportPresenter.class)
    private PatientDataReportPresenter presenter;

    private PatientDataAdapter patientDataAdapter;

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        patientDataAdapter = new PatientDataAdapter(this, presenter.getViewModels());
        rvPatientDataPeriods.setAdapter(patientDataAdapter);
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }

    @Override
    protected void loadForms() {

    }

}
