package org.openlmis.core.view.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.PatientDataReportFormPresenter;
import org.openlmis.core.view.adapter.PatientDataReportFormRowAdapter;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.functions.Action1;

public class PatientDataReportFormFragment extends BaseReportFragment {

    @InjectView(R.id.rv_patient_data_row_item_list)
    RecyclerView rvPatientDataRowItem;

    private PatientDataReportFormPresenter presenter;
    private PatientDataReportFormRowAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_data_report_form, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.generateViewModelsForAvailablePeriods();
        adapter = new PatientDataReportFormRowAdapter(presenter.getViewModels(), presenter);
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
