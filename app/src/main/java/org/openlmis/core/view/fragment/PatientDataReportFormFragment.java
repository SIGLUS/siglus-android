package org.openlmis.core.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import org.openlmis.core.enums.PatientDataReportType;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.PatientDataReportFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.MalariaDataReportFormRowAdapter;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

import static org.openlmis.core.model.PatientDataProgramStatus.DRAFT;
import static org.openlmis.core.model.PatientDataProgramStatus.SUBMITTED;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportFormFragment extends BaseReportFragment implements MalariaDataReportFormRowAdapter.PatientDataReportListener {

    @InjectView(R.id.rv_patient_data_row_item_list)
    private RecyclerView rvPatientDataRowItem;

    @Inject
    private MalariaDataReportFormRowAdapter adapter;

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
                ImplementationReportViewModel viewModel = implementationReportViewModels.get(0);
                if (viewModel.getStatus() == PatientDataProgramStatus.SYNCED) {
                    actionPanelView.setVisibility(View.GONE);
                } else if (patientPresenter.isSubmittedForApproval()) {
                    actionPanelView.getBtnComplete().setText(R.string.submit_for_approval);
                }
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
    protected Action1<? super Void> getOnSignedAction() {
        return null;
    }

    private SingleClickButtonListener getSaveFormListenerForStatus(final PatientDataProgramStatus status) {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                if (status.equals(SUBMITTED)) {
                    showSignDialog(status);
                } else {
                    onSaveForm(status, "");
                }
            }
        };
    }

    public void onSaveForm(PatientDataProgramStatus status, String sign) {
        Subscription subscription = patientPresenter.onSaveForm(status, sign).subscribe(getOnSaveActionForStatus(status));
        subscriptions.add(subscription);
    }

    @NonNull
    private Action1<? super MalariaProgram> getOnSaveActionForStatus(final PatientDataProgramStatus status) {
        return new Action1<MalariaProgram>() {
            @Override
            public void call(MalariaProgram malariaProgram) {
                ToastUtil.show(R.string.succesfully_saved);
                loaded();
                patientPresenter.setStatus(malariaProgram.getStatus());
                patientPresenter.setCreatedBy(malariaProgram.getCreatedBy());
                if (malariaProgram.getStatus().equals(DRAFT) && !malariaProgram.getCreatedBy().isEmpty()) {
                    showMessageNotifyDialog();
                    actionPanelView.getBtnComplete().setText(R.string.btn_complete);
                } else {
                    finishWithResult();
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        showConfirmLeaveDialog();
    }

    private void showConfirmLeaveDialog() {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Are you sure you want to leave?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishWithResult();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void finishWithResult() {
        Intent intent = new Intent();
        intent.putExtra("type", PatientDataReportType.MALARIA);
        getActivity().setResult(Activity.RESULT_OK, intent);
        finish();
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

    public void showSignDialog(PatientDataProgramStatus status) {
        SignatureDialog signatureDialog = new SignatureDialog();
        String signatureDialogTitle = getSignatureDialogTitle();
        signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
        signatureDialogDelegate = createDelegate(status);
        signatureDialog.setDelegate(signatureDialogDelegate);
        signatureDialog.show(this.getFragmentManager());
    }

    private SignatureDialog.DialogDelegate createDelegate(final PatientDataProgramStatus status) {
        return new SignatureDialog.DialogDelegate() {
            public void onSign(String sign) {
                if (actionPanelView.getBtnComplete().getText().toString().equals(getString(R.string.submit_for_approval))) {
                    onSaveForm(DRAFT, sign);
                } else {
                    onSaveForm(status, sign);
                }
            }
        };
    }

    protected String getSignatureDialogTitle() {
        return patientPresenter.isSubmittedForApproval() ? getResources().getString(R.string.msg_malaria_submit_signature) : getResources().getString(R.string.msg_approve_signature_malaria);
    }

    @Override
    public String getNotifyDialogMsg() {
        return getActivity().getString(R.string.malaria_report_notify_dialog);
    }
}
