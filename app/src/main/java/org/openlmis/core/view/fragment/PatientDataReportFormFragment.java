package org.openlmis.core.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.MalariaProgramStatus;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.PatientDataReportFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.PatientDataReportFormRowAdapter;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

import static org.openlmis.core.model.MalariaProgramStatus.DRAFT;
import static org.openlmis.core.model.MalariaProgramStatus.SUBMITTED;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportFormFragment extends BaseReportFragment implements PatientDataReportFormRowAdapter.PatientDataReportListener {

    @InjectView(R.id.rv_patient_data_row_item_list)
    RecyclerView rvPatientDataRowItem;

    @Inject
    private PatientDataReportFormRowAdapter adapter;

    @Inject
    private MalariaProgramRepository malariaProgramRepository;

    private Period period;
    private List<Subscription> subscriptions;
    private PatientDataReportFormPresenter patientPresenter;

    public PatientDataReportFormFragment() {
        subscriptions = newArrayList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DateTime periodBegin = (DateTime) this.getActivity().getIntent().getExtras().get(Constants.PARAM_PERIOD_BEGIN);
        period = new Period(periodBegin);
        return inflater.inflate(R.layout.fragment_patient_data_report_form, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter.setListener(this);
        rvPatientDataRowItem.setLayoutManager(new LinearLayoutManager(getActivity()));
        actionPanelView.setListener(getSaveFormListenerForStatus(SUBMITTED), getSaveFormListenerForStatus(DRAFT));
        rvPatientDataRowItem.setAdapter(adapter);
        Subscription subscription = patientPresenter.loadPatientData(period).subscribe(patientDataReportDataSubscriber());
        subscriptions.add(subscription);
    }

    private Action1<List<ImplementationReportViewModel>> patientDataReportDataSubscriber() {
        return new Action1<List<ImplementationReportViewModel>>() {
            @Override
            public void call(List<ImplementationReportViewModel> implementationReportViewModels) {
                adapter.setViewModels(implementationReportViewModels);
                adapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    protected BaseReportPresenter injectPresenter() {
        patientPresenter = RoboGuice.getInjector(getActivity()).getInstance(PatientDataReportFormPresenter.class);
        return patientPresenter;
    }

    @Override
    protected String getSignatureDialogTitle() {
        return null;
    }

    @Override
    protected Action1<? super Void> getOnSignedAction() {
        return null;
    }

    private SingleClickButtonListener getSaveFormListenerForStatus(final MalariaProgramStatus status) {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                onSaveForm(status);
            }
        };
    }


    public void onSaveForm(MalariaProgramStatus status) {
        Subscription subscription = patientPresenter.onSaveForm(status).subscribe(getOnSaveActionForStatus(status));
        subscriptions.add(subscription);
    }

    @NonNull
    private Action1<? super MalariaProgram> getOnSaveActionForStatus(final MalariaProgramStatus status) {
        return new Action1<MalariaProgram>() {
            @Override
            public void call(MalariaProgram malariaProgram) {
                ToastUtil.show(R.string.succesfully_saved);
                loaded();
                if (status == MalariaProgramStatus.SUBMITTED) {
                    finish();
                }
            }
        };
    }

    @Override
    protected String getNotifyDialogMsg() {
        return null;
    }

    @Override
    public void notifyModelChanged(ImplementationReportViewModel reportViewModel) {
        PatientDataReportFormPresenter patientPresenter = (PatientDataReportFormPresenter) presenter;
        List<ImplementationReportViewModel> viewModels = patientPresenter.regenerateImplementationModels(reportViewModel);
        adapter.setViewModels(viewModels);
    }

    @Override
    public void onDestroy() {
        for (Subscription subscription : subscriptions) {
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }
        super.onDestroy();
    }
}
