package org.openlmis.core.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.PatientDataReportPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.PatientDataReportAdapter;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;

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
        final Context context = this;
        patientDataAdapter = new PatientDataReportAdapter(context);
        rvPatientDataPeriods.setLayoutManager(new LinearLayoutManager(context));
        rvPatientDataPeriods.setAdapter(patientDataAdapter);
        presenter.getViewModels().subscribe(loadViewModels());
    }

    @NonNull
    private Action1<List<PatientDataReportViewModel>> loadViewModels() {
        return new Action1<List<PatientDataReportViewModel>>() {
            @Override
            public void call(List<PatientDataReportViewModel> patientDataReportViewModels) {
                patientDataAdapter.setViewModels(patientDataReportViewModels);
                patientDataAdapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }

    @Override
    protected void loadForms() {}

}
