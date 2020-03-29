/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */
package org.openlmis.core.view.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.ALRequisitionPresenter;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.ALReportAdapter;
import org.openlmis.core.view.holder.ALReportViewHolder;
import org.openlmis.core.view.viewmodel.ALGridViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.Date;

import roboguice.inject.InjectView;

import roboguice.RoboGuice;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

public class ALRequisitionFragment extends BaseReportFragment implements ALRequisitionPresenter.ALRequisitionView {

    private static final String TAG = ALRequisitionFragment.class.getSimpleName();
    private long formId;
    protected View containerView;
    private Date periodEndDate;
    ALRequisitionPresenter presenter;
    ALReportAdapter adapter;

    @InjectView(R.id.rv_al_row_item_list)
    RecyclerView rvALRowItemListView;

    @InjectView(R.id.al_monthTitle)
    TextView monthTitle;

    @InjectView(R.id.al_header)
    LinearLayout al_header;
    @InjectView(R.id.al_left_header_top)
    TextView al_header_top;
    @InjectView(R.id.al_left_header_chw)
    TextView al_header_chw;
    @InjectView(R.id.al_left_header_hf)
    TextView al_header_hf;
    @InjectView(R.id.al_left_header_total)
    TextView al_header_total;
    @InjectView(R.id.rv_al_row_item_list_container)
    RelativeLayout al_item_container;
    @InjectView(R.id.al_left_header)
    LinearLayout al_left_header;

    public static int LIST_ITEM_HEIGHT = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
        periodEndDate = ((Date) getActivity().getIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    }


    @Override
    protected BaseReportPresenter injectPresenter() {
        presenter = RoboGuice.getInjector(getActivity()).getInstance(ALRequisitionPresenter.class);
        return presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        containerView = inflater.inflate(R.layout.fragment_al_requisition, container, false);
        return containerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (SharedPreferenceMgr.getInstance().shouldSyncLastYearStockData()) {
            ToastUtil.showInCenter(R.string.msg_stock_movement_is_not_ready);
            finish();
            return;
        }

        initUI();
        setUpRowItems();
        updateHeaderSize();
        if (isSavedInstanceState && presenter.getRnRForm() != null) {
            presenter.updateFormUI();
        } else {
            presenter.loadData(formId, periodEndDate);
        }
    }

    private void updateHeaderSize() {
        al_header_top.setHeight(al_header.getLayoutParams().height);
        LIST_ITEM_HEIGHT = al_item_container.getHeight() / 3;
        al_header_chw.setHeight(LIST_ITEM_HEIGHT);
        al_header_hf.setHeight(LIST_ITEM_HEIGHT);
        al_header_total.setHeight(LIST_ITEM_HEIGHT);
    }

    protected void initUI() {
        if (isHistoryForm()) {
            actionPanelView.setVisibility(View.GONE);
        } else {
            actionPanelView.setVisibility(View.VISIBLE);
        }
        bindListeners();
    }


    private void bindListeners() {
        actionPanelView.setListener(getOnCompleteListener(), getOnSaveListener());
    }

    @NonNull
    private SingleClickButtonListener getOnSaveListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                loading();
                Subscription subscription = presenter.getSaveFormObservable()
                        .subscribe(getOnSavedSubscriber());
                subscriptions.add(subscription);
            }
        };
    }

    @NonNull
    public Subscriber<Void> getOnSavedSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                loaded();
                finish();
            }

            @Override
            public void onError(Throwable e) {
                loaded();
                ToastUtil.show(getString(R.string.hint_save_mmia_failed));
            }

            @Override
            public void onNext(Void aVoid) {

            }
        };
    }

    @NonNull
    private SingleClickButtonListener getOnCompleteListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                if (presenter.isComplete()) {
                    try {
                        presenter.setViewModels();
                    } catch (LMISException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (!presenter.validateFormPeriod()) {
                        ToastUtil.show(R.string.msg_requisition_not_unique);
                    } else {
                        showSignDialog();
                    }
                } else {
                    adapter.updateTip();
                    ToastUtil.showForLongTime(R.string.msg_uncomplete_hint);
                }
            }
        };
    }

    private boolean isHistoryForm() {
        return formId != 0;
    }


    @Override
    public void setProcessButtonName(String buttonName) {
        actionPanelView.setPositiveButtonText(buttonName);
    }

    @Override
    public void completeSuccess() {
        ToastUtil.showForLongTime(R.string.msg_al_submit_tip);
        finish();

    }

    @Override
    protected String getSignatureDialogTitle() {
        return presenter.isDraftOrDraftMissed() ? getResources().getString(R.string.msg_al_submit_signature)
                : getResources().getString(R.string.msg_approve_signature_al);
    }

    @Override
    protected Action1<? super Void> getOnSignedAction() {
        return new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (presenter.getRnRForm().isSubmitted()) {
                    presenter.submitRequisition();
                    showMessageNotifyDialog();
                } else {
                    presenter.authoriseRequisition();
                }
            }
        };
    }

    @Override
    protected String getNotifyDialogMsg() {
        return getString(R.string.msg_requisition_signature_message_notify_al);

    }

    @Override
    public void refreshRequisitionForm(RnRForm rnRForm) {
        getActivity().setTitle(getString(R.string.label_AL_title, DateUtil.formatDateWithoutYear(rnRForm.getPeriodBegin()), DateUtil.formatDateWithoutYear(rnRForm.getPeriodEnd())));
        monthTitle.setText(DateUtil.formatDateWithLongMonthAndYear(rnRForm.getPeriodEnd()));
        adapter.refresh(presenter.alReportViewModel);
    }

    @Override
    protected void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private void setUpRowItems() {
        adapter = new ALReportAdapter(getQuantityChangeListener());
        rvALRowItemListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvALRowItemListView.setNestedScrollingEnabled(false);
        rvALRowItemListView.setAdapter(adapter);
    }

    private ALReportViewHolder.QuantityChangeListener getQuantityChangeListener() {
        return new ALReportViewHolder.QuantityChangeListener() {
            @Override
            public void updateTotal(ALGridViewModel.ALColumnCode columnCode, ALGridViewModel.ALGridColumnCode gridColumnCode) {
                presenter.alReportViewModel.updateTotal(columnCode, gridColumnCode);
                adapter.updateTotal();
            }
        };
    }
}
