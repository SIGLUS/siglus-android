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
import android.widget.ScrollView;

import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.ALRequisitionPresenter;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.ALReportAdapter;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.Date;
import roboguice.inject.InjectView;

import roboguice.RoboGuice;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

public class ALRequisitionFragment extends BaseReportFragment implements ALRequisitionPresenter.ALRequisitionView {

    private long formId;
    protected View containerView;
    private Date periodEndDate;
    ALRequisitionPresenter presenter;
    ALReportAdapter adapter;

    @InjectView(R.id.scrollView)
    ScrollView scrollView;

    @InjectView(R.id.rv_al_row_item_list)
    RecyclerView rvALRowItemListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
        periodEndDate = ((Date) getActivity().getIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    }


    @Override
    protected BaseReportPresenter injectPresenter() {
        return RoboGuice.getInjector(getActivity()).getInstance(ALRequisitionPresenter.class);
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
        if (isSavedInstanceState && presenter.getRnRForm() != null) {
            presenter.updateFormUI();
        } else {
            presenter.loadData(formId, periodEndDate);
        }
    }

    protected void initUI() {
        scrollView.setVisibility(View.INVISIBLE);
        if (isHistoryForm()) {
            scrollView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            actionPanelView.setVisibility(View.GONE);
        } else {
            scrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
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
//                Subscription subscription = presenter.getSaveFormObservable(rnrFormList.itemFormList, regimeListView.getDataList(), mmiaInfoListView.getDataList(), etComment.getText().toString())
//                        .subscribe(getOnSavedSubscriber());
//                subscriptions.add(subscription);
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
//                if (rnrFormList.isCompleted() && regimeListView.isCompleted() && mmiaInfoListView.isCompleted()) {
//                    presenter.setViewModels(rnrFormList.itemFormList, regimeListView.getDataList(), mmiaInfoListView.getDataList(), etComment.getText().toString());
//                    if (!presenter.validateFormPeriod()) {
//                        ToastUtil.show(R.string.msg_requisition_not_unique);
//                    } else {
//                        showSignDialog();
//                    }
//                }
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
        return presenter.isDraftOrDraftMissed() ? getResources().getString(R.string.msg_al_submit_signature) :
                getResources().getString(R.string.msg_approve_signature_al);
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


    private void setUpRowItems() {
        adapter = new ALReportAdapter();
        rvALRowItemListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvALRowItemListView.setAdapter(adapter);
    }


    @Override
    public void refreshRequisitionForm(RnRForm rnRForm) {

    }

    @Override
    protected void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
