package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.enums.PatientDataReportType;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.presenter.PtvProgramPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.PTVProgramAdapter;
import org.openlmis.core.view.widget.ActionPanelView;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;

@Getter
@ContentView(R.layout.activity_ptv_report_form)
public class PTVDataReportFormActivity extends BaseActivity {

    @InjectView(R.id.tv_service_name1)
    TextView tvService1;

    @InjectView(R.id.tv_service_name2)
    TextView tvService2;

    @InjectView(R.id.tv_service_name3)
    TextView tvService3;

    @InjectView(R.id.tv_service_name4)
    TextView tvService4;

    @InjectView(R.id.tv_service_name5)
    TextView tvService5;

    @InjectView(R.id.tv_service_name6)
    TextView tvService6;

    @InjectView(R.id.tv_service_name7)
    TextView tvService7;

    @InjectView(R.id.tv_service_name8)
    TextView tvService8;

    @InjectView(R.id.rv_ptv_report_information)
    RecyclerView rvPtvReportInformation;

    @InjectView(R.id.action_panel)
    ActionPanelView actionPanelView;

    @InjectView(R.id.et_total_woman)
    EditText etTotalWoman;

    @InjectView(R.id.et_total_child)
    EditText etTotalChild;

    @InjectPresenter(PtvProgramPresenter.class)
    PtvProgramPresenter ptvProgramPresenter;

    private boolean isCompleted;

    private Period period;
    private PTVProgramAdapter ptvProgramAdapter;

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loading(getString(R.string.loading_report_information));
        initializePtvProgramPresenter();
    }

    private void initializePtvProgramPresenter() {
        DateTime periodBegin = (DateTime) getIntent().getSerializableExtra(Constants.PARAM_PERIOD_BEGIN);
        period = new Period(periodBegin);
        ptvProgramPresenter.setPeriod(period);
        ptvProgramPresenter.buildInitialPtvProgram().subscribe(getInitialPTVProgramSubscriber());
        actionPanelView.setListener(addCompleteListener(), addSaveListener());
    }

    @NonNull
    private View.OnClickListener addSaveListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePTVProgram();
                isCompleted = false;
                changeButtonsState(false);
                ptvProgramPresenter.savePTVProgram(isCompleted).subscribe(updatePTVProgramSubscriber());
            }
        };
    }

    @NonNull
    private Subscriber<PTVProgram> updatePTVProgramSubscriber() {
        return new Subscriber<PTVProgram>() {
            @Override
            public void onCompleted() {
                Toast.makeText(getApplicationContext(), R.string.succesfully_saved, Toast.LENGTH_LONG).show();
                if (isCompleted) {
                    finishWithResult();
                }
                changeButtonsState(true);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                new LMISException(e.getCause()).reportToFabric();
            }

            @Override
            public void onNext(PTVProgram ptvProgram) {
            }
        };
    }

    @NonNull
    private View.OnClickListener addCompleteListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePTVProgram();
                changeButtonsState(false);
                isCompleted = true;
                ptvProgramPresenter.savePTVProgram(isCompleted).subscribe(updatePTVProgramSubscriber());
            }
        };
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
    }

    private void finishWithResult() {
        Intent intent = new Intent();
        intent.putExtra("type", PatientDataReportType.PTV);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void updatePTVProgram() {
        String totalWoman = etTotalWoman.getText().toString();
        String totalChild = etTotalChild.getText().toString();
        ptvProgramPresenter.updatePTVProgram(ptvProgramAdapter.getPtvProgramStocksInformation(), totalWoman, totalChild);
    }

    private void initializeRecyclerView(PTVProgram ptvProgram) {
        rvPtvReportInformation.setLayoutManager(new LinearLayoutManager(this));
        ptvProgramAdapter = new PTVProgramAdapter(ptvProgram);
        rvPtvReportInformation.setAdapter(ptvProgramAdapter);
        ptvProgramAdapter.refresh();
    }

    @NonNull
    private Subscriber<PTVProgram> getInitialPTVProgramSubscriber() {
        return new Subscriber<PTVProgram>() {
            @Override
            public void onCompleted() {
                loaded();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(PTVProgram ptvProgram) {
                updateHeader(ptvProgram);
                initializeRecyclerView(ptvProgram);
            }
        };
    }

    public static Intent getIntentToMe(Context context, DateTime periodBegin) {
        Intent intent = new Intent(context, PTVDataReportFormActivity.class);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        return intent;
    }

    public void updateHeader(PTVProgram ptvProgram) {
        if(ptvProgram.getStatus().equals(PatientDataProgramStatus.SUBMITTED)){
            actionPanelView.setVisibility(View.GONE);
            etTotalChild.setClickable(false);
            etTotalWoman.setClickable(false);
            etTotalWoman.setFocusable(false);
            etTotalChild.setFocusable(false);
            rvPtvReportInformation.setClickable(false);
            rvPtvReportInformation.setFocusable(false);
        }
        setHealthFacilityServicesNames(ptvProgram);
        setPatientDispensationValues(ptvProgram);
    }

    private void setHealthFacilityServicesNames(PTVProgram ptvProgram) {
        List<PTVProgramStockInformation> ptvProgramStocksInformation = new ArrayList<>(ptvProgram.getPtvProgramStocksInformation());
        TextView[] tvServices = {tvService1, tvService2, tvService3, tvService4, tvService5, tvService6, tvService7, tvService8};
        List<ServiceDispensation> servicesDispensations = new ArrayList<>(ptvProgramStocksInformation.get(0).getServiceDispensations());
        for (int i = 0; i < tvServices.length; i++) {
            HealthFacilityService healthFacilityService = servicesDispensations.get(i).getHealthFacilityService();
            tvServices[i].setText(healthFacilityService.getName());
        }
    }

    private void setPatientDispensationValues(PTVProgram ptvProgram) {
        for (PatientDispensation patientDispensation : ptvProgram.getPatientDispensations()) {
            if (patientDispensation.getType().equals(PatientDispensation.Type.CHILD)) {
                etTotalChild.setText(String.valueOf(patientDispensation.getTotal()));
            } else if (patientDispensation.getType().equals(PatientDispensation.Type.WOMAN)) {
                etTotalWoman.setText(String.valueOf(patientDispensation.getTotal()));
            }
        }
    }

    private void changeButtonsState(boolean isEnabled) {
        actionPanelView.getBtnComplete().setFocusable(isEnabled);
        actionPanelView.getBtnSave().setFocusable(isEnabled);
        actionPanelView.getBtnComplete().setClickable(isEnabled);
        actionPanelView.getBtnSave().setClickable(isEnabled);
    }
}
