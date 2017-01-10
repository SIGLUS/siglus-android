package org.openlmis.core.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.RapidTestReportFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.RapidTestReportRowAdapter;
import org.openlmis.core.view.holder.RapidTestReportGridViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

public class RapidTestReportFormFragment extends BaseReportFragment {
    @InjectView(R.id.rv_rapid_report_row_item_list)
    RecyclerView rvReportRowItemListView;

    @InjectView(R.id.vg_rapid_test_report_empty_header)
    ViewGroup emptyHeaderView;

    RapidTestReportFormPresenter presenter;

    RapidTestReportRowAdapter adapter;

    public static int ROW_HEADER_WIDTH = -1;

    public static int GRID_SIZE = -1;

    @Override
    protected BaseReportPresenter injectPresenter() {
        presenter = RoboGuice.getInjector(getActivity()).getInstance(RapidTestReportFormPresenter.class);
        return presenter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        long formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0L);
        DateTime periodBegin = (DateTime) getActivity().getIntent().getSerializableExtra(Constants.PARAM_PERIOD_BEGIN);

        updateHeaderSize();
        setUpRowItems();
        if (isSavedInstanceState && presenter.getViewModel() != null) {
            updateUI();
        } else {
            loadForm(formId, periodBegin);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rapid_test_report_form, container, false);
    }

    private void updateHeaderSize() {
        calculateRowHeaderAndGridSize();
        emptyHeaderView.getLayoutParams().width = ROW_HEADER_WIDTH;
    }

    private void calculateRowHeaderAndGridSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int totalWidthWithoutBorders = metrics.widthPixels - 2;
        GRID_SIZE = totalWidthWithoutBorders / 5;
        ROW_HEADER_WIDTH = GRID_SIZE + totalWidthWithoutBorders % 5;
    }

    private void loadForm(long formId, DateTime periodBegin) {
        loading();
        Subscription subscription = presenter.loadViewModel(formId, periodBegin).subscribe(getOnViewModelLoadedAction());
        subscriptions.add(subscription);
    }

    private void setUpRowItems() {
        adapter = new RapidTestReportRowAdapter(getQuantityChangeListener());
        rvReportRowItemListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvReportRowItemListView.setAdapter(adapter);
    }

    private RapidTestReportGridViewHolder.QuantityChangeListener getQuantityChangeListener() {
        return new RapidTestReportGridViewHolder.QuantityChangeListener() {
            @Override
            public void updateTotal(RapidTestFormGridViewModel.ColumnCode columnCode, boolean isConsume) {
                presenter.getViewModel().updateTotal(columnCode, isConsume);
                adapter.updateTotal();
            }
        };
    }

    private void setUpButtonPanel() {
        actionPanelView.setVisibility(presenter.getViewModel().getStatus().isEditable() ? View.VISIBLE : View.GONE);
        updateButtonName();
        actionPanelView.setListener(getOnCompleteClickListener(), getOnSaveClickListener());
    }

    @NonNull
    private SingleClickButtonListener getOnSaveClickListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                onSaveForm();
            }
        };
    }

    @NonNull
    private SingleClickButtonListener getOnCompleteClickListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                if (presenter.getViewModel().isFormEmpty()) {
                    ToastUtil.show(getString(R.string.error_empty_rapid_test));
                } else if (!presenter.getViewModel().validate()) {
                    ToastUtil.show(getString(R.string.error_positive_larger_than_consumption));
                } else {
                    showSignDialog();
                }
            }
        };
    }

    public void onSaveForm() {
        loading();
        Subscription subscription = presenter.onSaveDraftForm().subscribe(getSavedSubscriber());
        subscriptions.add(subscription);
    }

    private void updateButtonName() {
        actionPanelView.setPositiveButtonText(presenter.getViewModel().isDraft() ? getResources().getString(R.string.btn_submit) : getResources().getString(R.string.btn_complete));
    }

    private void showSignDialog() {
        SignatureDialog signatureDialog = new SignatureDialog();
        String signatureDialogTitle = presenter.getViewModel().isDraft() ? getResources().getString(R.string.msg_rapid_test_submit_signature) : getResources().getString(R.string.msg_approve_signature_rapid_test);

        signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
        signatureDialog.setDelegate(signatureDialogDelegate);

        signatureDialog.show(getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        public void onSign(String sign) {
            Subscription subscription = presenter.onSignObservable(sign).subscribe(getOnSignedAction());
            subscriptions.add(subscription);
        }
    };

    private Action1<? super Void> getOnSignedAction() {
        return new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (presenter.getViewModel().isAuthorized()) {
                    onSaveForm();
                } else {
                    showMessageNotifyDialog();
                    presenter.saveForm();
                    updateUIAfterSubmit();
                }
            }
        };
    }

    public void updateUIAfterSubmit() {
        adapter.setEditable(false);
        adapter.notifyDataSetChanged();
        updateButtonName();
    }

    public void showMessageNotifyDialog() {
        SimpleDialogFragment notifyDialog = SimpleDialogFragment.newInstance(null,
                getString(R.string.msg_requisition_signature_message_notify_rapid_test), null, getString(R.string.btn_continue), "showMessageNotifyDialog");

        notifyDialog.show(getActivity().getFragmentManager(), "showMessageNotifyDialog");
    }

    private Action1<? super RapidTestReportViewModel> getSavedSubscriber() {
        return new Action1<RapidTestReportViewModel>() {
            @Override
            public void call(RapidTestReportViewModel viewModel) {
                loaded();
                finish();
            }
        };
    }

    @NonNull
    private Action1<RapidTestReportViewModel> getOnViewModelLoadedAction() {
        return new Action1<RapidTestReportViewModel>() {
            @Override
            public void call(RapidTestReportViewModel viewModel) {
                updateUI();
                loadMessageDialogIfIsDraft();
                loaded();
            }
        };
    }

    public void updateUI() {
        populateFormData(presenter.getViewModel());
        setUpButtonPanel();
    }

    private void loadMessageDialogIfIsDraft() {
        if (presenter.isSubmitted()) {
            showMessageNotifyDialog();
        }
    }

    private void populateFormData(RapidTestReportViewModel viewModel) {
        adapter.refresh(viewModel.getItemViewModelList(), viewModel.isEditable());
    }
}
