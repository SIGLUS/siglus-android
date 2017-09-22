package org.openlmis.core.view.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.model.Period;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.PatientDataReportFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.adapter.PatientDataReportFormRowAdapter;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.functions.Action1;

public class PatientDataReportFormFragment extends BaseReportFragment {

    @InjectView(R.id.rv_patient_data_row_item_list)
    RecyclerView rvPatientDataRowItem;

    private PatientDataReportFormPresenter presenter;
    private PatientDataReportFormRowAdapter adapter;
    private Period periodBegin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DateTime period = (DateTime) this.getActivity().getIntent().getExtras().get(Constants.PARAM_PERIOD_BEGIN);
        this.periodBegin = new Period(period);
        return inflater.inflate(R.layout.fragment_patient_data_report_form, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.generateViewModelsBySpecificPeriod(periodBegin);
        adapter = new PatientDataReportFormRowAdapter(presenter.getViewModels(periodBegin), presenter, periodBegin);
        rvPatientDataRowItem.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPatientDataRowItem.setAdapter(adapter);
    }

    @Override
    protected BaseReportPresenter injectPresenter() {
        presenter = RoboGuice.getInjector(getActivity()).getInstance(PatientDataReportFormPresenter.class);
        return presenter;
    }

    @Override
    protected String getSignatureDialogTitle() {
        return null;
    }

    @Override
    protected Action1<? super Void> getOnSignedAction() {
        return null;
    }

    @Override
    protected String getNotifyDialogMsg() {
        return null;
    }
}
