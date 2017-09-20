package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.PatientDataReportAdapter;
import org.openlmis.core.presenter.PatientDataReportPresenter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_patient_data_report)
public class PatientDataReportActivity extends BaseReportListActivity {

    @InjectPresenter(PatientDataReportPresenter.class)
    PatientDataReportPresenter presenter;

    @InjectView(R.id.rv_patient_data_periods)
    private RecyclerView rvPatientDataPeriods;

    private PatientDataReportAdapter patientDataAdapter;

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        patientDataAdapter = new PatientDataReportAdapter(this, presenter.getViewModels());
        System.out.println(presenter.getViewModels().size());
        System.out.println(presenter.getViewModels().get(0).getPeriod());
        rvPatientDataPeriods.setLayoutManager(new LinearLayoutManager(this));
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
