package org.openlmis.core.view.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.RapidTestReportFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.RapidTestReportRowAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.openlmis.core.view.widget.SignatureDialog;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

@ContentView(R.layout.activity_rapid_test_report_form)
public class RapidTestReportFormActivity extends BaseActivity implements SimpleDialogFragment.MsgDialogCallBack {
    @InjectView(R.id.rv_rapid_report_row_item_list)
    RecyclerView rvReportRowItemListView;

    @InjectView(R.id.btn_complete)
    Button btnComplete;

    @InjectView(R.id.btn_save)
    View btnSave;

    @InjectView(R.id.action_panel)
    View actionPanel;

    @InjectPresenter(RapidTestReportFormPresenter.class)
    RapidTestReportFormPresenter presenter;

    RapidTestReportRowAdapter adapter;
    private SignatureDialog signatureDialog;
    private SimpleDialogFragment notifyDialog;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.RapidTestReportFormScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signatureDialog = null;
        long formId = getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0L);
        DateTime periodBegin = (DateTime) getIntent().getSerializableExtra(Constants.PARAM_PERIOD_BEGIN);

        setUpRowItems();
        loadForm(formId, periodBegin);
    }

    private void loadForm(long formId, DateTime periodBegin) {
        loading();
        Subscription subscription = presenter.loadViewModel(formId, periodBegin).subscribe(getPopulateFormDataAction());
        subscriptions.add(subscription);
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_BlueGray;
    }

    private void setUpRowItems() {
        adapter = new RapidTestReportRowAdapter();
        rvReportRowItemListView.setLayoutManager(new LinearLayoutManager(this));
        rvReportRowItemListView.setAdapter(adapter);
    }

    private void setUpButtonPanel() {
        actionPanel.setVisibility(presenter.getViewModel().getStatus().isEditable() ? View.VISIBLE : View.GONE);
        updateButtonName();
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter.getViewModel().isFormEmpty()) {
                    ToastUtil.show(getString(R.string.error_empty_rapid_test));
                } else if (!presenter.getViewModel().validate()) {
                    ToastUtil.show(getString(R.string.error_positive_larger_than_consumption));
                } else {
                    showSignDialog();
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveForm();
            }
        });
    }

    public void onSaveForm() {
        loading();
        Subscription subscription = presenter.onSaveDraftForm().subscribe(getSavedSubscriber());
        subscriptions.add(subscription);
    }

    private void updateButtonName() {
        if (presenter.getViewModel().isDraft()) {
            btnComplete.setText(getResources().getString(R.string.btn_submit));
        } else {
            btnComplete.setText(getResources().getString(R.string.btn_complete));
        }
    }

    private void showSignDialog() {
        signatureDialog = new SignatureDialog();
        String signatureDialogTitle = presenter.getViewModel().isDraft() ? getResources().getString(R.string.msg_rapid_test_submit_signature) : getResources().getString(R.string.msg_approve_signature_rapid_test);

        signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
        signatureDialog.setDelegate(signatureDialogDelegate);

        signatureDialog.show(getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        public void onSign(String sign) {
            Subscription subscription = presenter.sign(sign).subscribe(new Action1<RapidTestReportViewModel>() {
                @Override
                public void call(RapidTestReportViewModel viewModel) {
                    if (viewModel.isAuthorized()) {
                        onSaveForm();
                    } else {
                        showMessageNotifyDialog();
                        presenter.saveForm();
                        adapter.setEditable(false);
                        adapter.notifyDataSetChanged();
                        updateButtonName();
                    }
                }
            });
            subscriptions.add(subscription);
        }
    };

    public void showMessageNotifyDialog() {
        notifyDialog = SimpleDialogFragment.newInstance(null,
                getString(R.string.msg_requisition_signature_message_notify_rapid_test), null, getString(R.string.btn_continue), "showMessageNotifyDialog");

        notifyDialog.show(getFragmentManager(), "showMessageNotifyDialog");
    }

    private Action1<? super RapidTestReportViewModel> getSavedSubscriber() {
        return new Action1<RapidTestReportViewModel>() {
            @Override
            public void call(RapidTestReportViewModel viewModel) {
                loaded();
                RapidTestReportFormActivity.this.finish();
            }
        };
    }

    @NonNull
    private Action1<RapidTestReportViewModel> getPopulateFormDataAction() {
        return new Action1<RapidTestReportViewModel>() {
            @Override
            public void call(RapidTestReportViewModel viewModel) {
                populateFormData(viewModel);
                setUpButtonPanel();
                loadMessageDialogIfIsDraft();
                loaded();
            }
        };
    }

    private void loadMessageDialogIfIsDraft() {
        if (presenter.isSubmitted()) {
            showMessageNotifyDialog();
        }
    }

    private void populateFormData(RapidTestReportViewModel viewModel) {
        adapter.refresh(viewModel.getItemViewModelList(), viewModel.isEditable());
    }

    public static Intent getIntentToMe(Context context, long formId, DateTime periodBegin) {
        Intent intent = new Intent(context, RapidTestReportFormActivity.class);
        intent.putExtra(Constants.PARAM_FORM_ID, formId);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        return intent;
    }

    @Override
    public void onBackPressed() {
        if (presenter.getViewModel().isEditable()) {
            showConfirmDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showConfirmDialog() {
        DialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                null,
                getString(R.string.msg_back_confirm),
                getString(R.string.btn_positive),
                getString(R.string.btn_negative),
                "onBackPressed");
        dialogFragment.show(getFragmentManager(), "");
    }

    @Override
    public void positiveClick(String tag) {
        presenter.deleteDraft();
        super.onBackPressed();
    }

    @Override
    public void negativeClick(String tag) {
    }

    @Override
    protected void onPause() {
        if (signatureDialog != null) {
            signatureDialog.dismiss();
        }
        if (notifyDialog != null) {
            notifyDialog.dismiss();
        }
        super.onPause();
    }
}
