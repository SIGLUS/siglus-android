package org.openlmis.core.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.PtvProgramPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.PTVProgramAdapter;
import org.openlmis.core.view.widget.ActionPanelView;
import org.openlmis.core.view.widget.SignatureDialog;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@Getter
@ContentView(R.layout.activity_ptv_report_form)
public class PTVDataReportFormActivity extends BaseActivity {

    @InjectView(R.id.tv_product_name1)
    TextView tvProduct1;

    @InjectView(R.id.tv_product_name2)
    TextView tvProduct2;

    @InjectView(R.id.tv_product_name3)
    TextView tvProduct3;

    @InjectView(R.id.tv_product_name4)
    TextView tvProduct4;

    @InjectView(R.id.tv_product_name5)
    TextView tvProduct5;

    @InjectView(R.id.tv_initial_stock1)
    TextView tvInitialStock1;

    @InjectView(R.id.tv_initial_stock2)
    TextView tvInitialStock2;

    @InjectView(R.id.tv_initial_stock3)
    TextView tvInitialStock3;

    @InjectView(R.id.tv_initial_stock4)
    TextView tvInitialStock4;

    @InjectView(R.id.tv_initial_stock5)
    TextView tvInitialStock5;

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
                ptvProgramPresenter.savePTVProgram(isCompleted).subscribe(savePTVProgramSubscriber());
            }
        };
    }

    @NonNull
    private Subscriber<PTVProgram> savePTVProgramSubscriber() {
        return new Subscriber<PTVProgram>() {
            @Override
            public void onCompleted() {
                Toast.makeText(getApplicationContext(), R.string.succesfully_saved, Toast.LENGTH_LONG).show();
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
    private Subscriber<PTVProgram> updatePTVProgramSubscriber() {
        return new Subscriber<PTVProgram>() {
            @Override
            public void onCompleted() {
                Toast.makeText(getApplicationContext(), R.string.succesfully_saved, Toast.LENGTH_LONG).show();
                if (isCompleted) {
                    finishWithResult();
                } else {
                    actionPanelView.getBtnComplete().setText(R.string.btn_complete);
                    changeButtonsState(true);
                    showMessageNotifyDialog();
                }
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
                showSignDialog();
                changeButtonsState(false);
            }
        };
    }

    @Override
    public void onBackPressed() {
        showConfirmLeaveDialog();
    }

    private void showConfirmLeaveDialog() {
        new AlertDialog.Builder(this)
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
        intent.putExtra("type", PatientDataReportType.PTV);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void updatePTVProgram() {
        String totalWoman = etTotalWoman.getText().toString();
        String totalChild = etTotalChild.getText().toString();
        ptvProgramPresenter.updatePTVProgram(totalWoman, totalChild);
    }

    private void initializeRecyclerView(PTVProgram ptvProgram) throws LMISException {
        rvPtvReportInformation.setLayoutManager(new LinearLayoutManager(this));
        ptvProgramAdapter = new PTVProgramAdapter(ptvProgram, ptvProgramPresenter.getViewModels());
        rvPtvReportInformation.setAdapter(ptvProgramAdapter);
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
                if (ptvProgramPresenter.isNotSubmittedForApproval()) {
                    actionPanelView.getBtnComplete().setText(R.string.submit_for_approval);
                }
                updateHeader(ptvProgram);
                try {
                    initializeRecyclerView(ptvProgram);
                } catch (LMISException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static Intent getIntentToMe(Context context, DateTime periodBegin) {
        Intent intent = new Intent(context, PTVDataReportFormActivity.class);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        return intent;
    }

    public void updateHeader(PTVProgram ptvProgram) {
        if (ptvProgram.getStatus().equals(PatientDataProgramStatus.SUBMITTED)) {
            actionPanelView.setVisibility(View.GONE);
            etTotalChild.setClickable(false);
            etTotalWoman.setClickable(false);
            etTotalWoman.setFocusable(false);
            etTotalChild.setFocusable(false);
            rvPtvReportInformation.setClickable(false);
            rvPtvReportInformation.setFocusable(false);
        }
        setProductsInformation(ptvProgram);
        setPatientDispensationValues(ptvProgram);
    }

    private void setProductsInformation(PTVProgram ptvProgram) {
        List<PTVProgramStockInformation> ptvProgramStocksInformation = new ArrayList<>(ptvProgram.getPtvProgramStocksInformation());
        TextView[] tvProducts = {tvProduct1, tvProduct2, tvProduct3, tvProduct4, tvProduct5};
        TextView[] tvInitialStock = {tvInitialStock1, tvInitialStock2, tvInitialStock3, tvInitialStock4, tvInitialStock5};
        for (int i = 0; i < tvProducts.length; i++) {
            PTVProgramStockInformation ptvProgramStockInformation = ptvProgramStocksInformation.get(i);
            Product product = ptvProgramStockInformation.getProduct();
            tvProducts[i].setText(product.getPrimaryName());
            tvInitialStock[i].setText(String.valueOf(ptvProgramStockInformation.getInitialStock()));
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

    public void showSignDialog() {
        SignatureDialog signatureDialog = new SignatureDialog();
        String signatureDialogTitle = getSignatureDialogTitle();
        signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
        signatureDialog.setDelegate(signatureDialogDelegate);
        signatureDialog.show(this.getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        public void onSign(String sign) {
            isCompleted = !ptvProgramPresenter.isNotSubmittedForApproval();
            Subscription subscription = ptvProgramPresenter.signReport(sign, isCompleted).subscribe(updatePTVProgramSubscriber());
            subscriptions.add(subscription);
        }
        public void onCancel() {
            changeButtonsState(true);
        }
    };

    protected String getSignatureDialogTitle() {
        return ptvProgramPresenter.isNotSubmittedForApproval() ? getResources().getString(R.string.msg_ptv_submit_signature) : getResources().getString(R.string.msg_approve_signature_ptv);
    }


    private void showMessageNotifyDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(R.string.ptv_report_notify_dialog))
                .setNegativeButton("Continue", changeButtonStateListener())
                .show();
    }

    @NonNull
    private DialogInterface.OnClickListener changeButtonStateListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeButtonsState(true);
            }
        };
    }

}
