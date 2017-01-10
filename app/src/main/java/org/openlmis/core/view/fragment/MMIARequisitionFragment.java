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
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.ViewUtil;
import org.openlmis.core.view.widget.ActionPanelView;
import org.openlmis.core.view.widget.MMIAInfoList;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARnrForm;
import org.openlmis.core.view.widget.RnrFormHorizontalScrollView;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.Date;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

public class MMIARequisitionFragment extends BaseReportFragment implements MMIARequisitionPresenter.MMIARequisitionView {
    @InjectView(R.id.rnr_form_list)
    protected MMIARnrForm rnrFormList;

    @InjectView(R.id.regime_list)
    protected MMIARegimeList regimeListView;

    @InjectView(R.id.mmia_info_list)
    protected MMIAInfoList mmiaInfoListView;

    @InjectView(R.id.tv_regime_total)
    protected TextView tvRegimeTotal;

    @InjectView(R.id.et_comment)
    protected TextView etComment;

    @InjectView(R.id.scrollview)
    protected ScrollView scrollView;

    @InjectView(R.id.tv_total_mismatch)
    protected TextView tvMismatch;

    @InjectView(R.id.mmia_rnr_items_header_freeze)
    protected ViewGroup rnrItemsHeaderFreeze;

    @InjectView(R.id.mmia_rnr_items_header_freeze_left)
    protected ViewGroup rnrItemsHeaderFreezeLeft;

    @InjectView(R.id.mmia_rnr_items_header_freeze_right)
    protected ViewGroup rnrItemsHeaderFreezeRight;

    @InjectView(R.id.action_panel)
    protected ActionPanelView actionPanel;

    MMIARequisitionPresenter presenter;

    private long formId;
    protected View containerView;
    private Date periodEndDate;

    protected static final String TAG_BACK_PRESSED = "onBackPressed";
    private static final String TAG_MISMATCH = "mismatch";
    private static final String TAG_SHOW_MESSAGE_NOTIFY_DIALOG = "showMessageNotifyDialog";

    public static final int REQUEST_FOR_CUSTOM_REGIME = 100;

    protected int actionBarHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
        periodEndDate = ((Date) getActivity().getIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    }

    @Override
    protected BaseReportPresenter injectPresenter() {
        presenter = RoboGuice.getInjector(getActivity()).getInstance(MMIARequisitionPresenter.class);
        return presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        containerView = inflater.inflate(R.layout.fragment_mmia_requisition, container, false);
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
            actionPanel.setVisibility(View.GONE);
            etComment.setEnabled(false);
        } else {
            scrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            actionPanel.setVisibility(View.VISIBLE);
            etComment.setEnabled(true);
        }
        disableFreezeHeaderScroll();
        initActionBarHeight();
        setRegimenListener();
    }

    private boolean isHistoryForm() {
        return formId != 0;
    }

    private void setRegimenListener() {
        regimeListView.setRegimeListener(new MMIARegimeList.MMIARegimeListener() {
            @Override
            public void loading() {
                MMIARequisitionFragment.this.loading();
            }

            @Override
            public void loaded() {
                MMIARequisitionFragment.this.loaded();
            }
        });
    }

    private void disableFreezeHeaderScroll() {
        rnrItemsHeaderFreezeRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void refreshRequisitionForm(RnRForm form) {
        scrollView.setVisibility(View.VISIBLE);
        rnrFormList.initView(form.getRnrFormItemListWrapper());
        regimeListView.initView(tvRegimeTotal, presenter);
        mmiaInfoListView.initView(form.getBaseInfoItemListWrapper());
        InflateFreezeHeaderView();
        getActivity().setTitle(getString(R.string.label_mmia_title, DateUtil.formatDateWithoutYear(form.getPeriodBegin()), DateUtil.formatDateWithoutYear(form.getPeriodEnd())));
        etComment.setText(form.getComments());
        highlightTotalDifference();
        bindListeners();
    }

    private void InflateFreezeHeaderView() {
        final View leftHeaderView = rnrFormList.getLeftHeaderView();
        rnrItemsHeaderFreezeLeft.addView(leftHeaderView);

        final ViewGroup rightHeaderView = rnrFormList.getRightHeaderView();
        rnrItemsHeaderFreezeRight.addView(rightHeaderView);

        rnrFormList.post(new Runnable() {
            @Override
            public void run() {
                ViewUtil.syncViewHeight(leftHeaderView, rightHeaderView);
            }
        });
    }


    protected void bindListeners() {
        etComment.post(new Runnable() {
            @Override
            public void run() {
                etComment.addTextChangedListener(commentTextWatcher);
            }
        });
        tvRegimeTotal.post(new Runnable() {
            @Override
            public void run() {
                tvRegimeTotal.addTextChangedListener(totalTextWatcher);
            }
        });
        final EditText patientTotalView = mmiaInfoListView.getPatientTotalView();
        patientTotalView.post(new Runnable() {
            @Override
            public void run() {
                patientTotalView.addTextChangedListener(totalTextWatcher);
            }
        });

        actionPanel.setListener(getOnCompleteListener(), getOnSaveListener());
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.requestFocus();
                hideImm();
                return false;
            }
        });

        bindFreezeHeaderListener();
    }

    @NonNull
    private SingleClickButtonListener getOnSaveListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                loading();
                Subscription subscription = presenter.getSaveFormObservable(regimeListView.getDataList(), mmiaInfoListView.getDataList(), etComment.getText().toString())
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
                if (regimeListView.isCompleted() && mmiaInfoListView.isCompleted()) {
                    presenter.setViewModels(regimeListView.getDataList(), mmiaInfoListView.getDataList(), etComment.getText().toString());
                    if (!presenter.validateForm()) {
                        showValidationAlert();
                    } else if (!presenter.validateFormPeriod()) {
                        ToastUtil.show(R.string.msg_requisition_not_unique);
                    } else {
                        showSignDialog();
                    }
                }
            }
        };
    }

    private void bindFreezeHeaderListener() {
        ViewTreeObserver verticalViewTreeObserver = scrollView.getViewTreeObserver();
        verticalViewTreeObserver.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                hideOrDisplayRnrItemsHeader();
            }
        });

        rnrFormList.getRnrItemsHorizontalScrollView().setOnScrollChangedListener(new RnrFormHorizontalScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                rnrItemsHeaderFreezeRight.scrollBy(l - oldl, 0);
            }
        });
    }

    private void initActionBarHeight() {
        containerView.post(new Runnable() {
            @Override
            public void run() {
                int[] initialTopLocationOfRnrForm = new int[2];
                containerView.getLocationOnScreen(initialTopLocationOfRnrForm);
                actionBarHeight = initialTopLocationOfRnrForm[1];
            }
        });
    }

    protected void hideOrDisplayRnrItemsHeader() {
        rnrItemsHeaderFreeze.setVisibility(isNeedHideFreezeHeader() ? View.INVISIBLE : View.VISIBLE);
    }

    private boolean isNeedHideFreezeHeader() {
        int[] rnrItemsViewLocation = new int[2];
        rnrFormList.getLocationOnScreen(rnrItemsViewLocation);
        final int rnrFormY = rnrItemsViewLocation[1];

        int lastItemHeight = rnrFormList.getRightViewGroup().getChildAt(rnrFormList.getRightViewGroup().getChildCount() - 1).getHeight();

        final int offsetY = -rnrFormY + rnrItemsHeaderFreeze.getHeight() + actionBarHeight;

        final int hiddenThresholdY = rnrFormList.getHeight() - lastItemHeight;

        return offsetY > hiddenThresholdY;
    }

    TextWatcher commentTextWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            highlightTotalDifference();
            presenter.setComments(s.toString());
        }
    };

    TextWatcher totalTextWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            highlightTotalDifference();
        }
    };

    private void highlightTotalDifference() {
        if (isHistoryForm() || hasEmptyColumn() || isTotalEqual() || etComment.getText().toString().length() >= 5) {
            regimeListView.deHighLightTotal();
            mmiaInfoListView.deHighLightTotal();
            tvMismatch.setVisibility(View.INVISIBLE);
        } else {
            regimeListView.highLightTotal();
            mmiaInfoListView.highLightTotal();
            tvMismatch.setVisibility(View.VISIBLE);
        }
    }

    private boolean hasEmptyColumn() {
        return regimeListView.hasEmptyField() || mmiaInfoListView.hasEmptyField();
    }

    public void onBackPressed() {
        if (presenter.getRnrFormStatus() == RnRForm.STATUS.DRAFT) {
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(null, getString(R.string.msg_back_confirm), getString(R.string.btn_positive), getString(R.string.btn_negative), TAG_BACK_PRESSED);
            dialogFragment.show(getActivity().getFragmentManager(), "back_confirm_dialog");
            dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
                @Override
                public void positiveClick(String tag) {
                    if (tag.equals(TAG_BACK_PRESSED)) {
                        presenter.removeRequisition();
                        finish();
                    }
                }

                @Override
                public void negativeClick(String tag) {
                }
            });
        } else {
            finish();
        }
    }

    @Override
    protected void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        super.finish();
    }

    @Override
    public void showValidationAlert() {
        DialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                getString(R.string.msg_regime_total_and_patient_total_not_match),
                getString(R.string.btn_ok),
                TAG_MISMATCH);
        dialogFragment.show(getFragmentManager(), "not_match_dialog");
    }

    @Override
    public void setProcessButtonName(String buttonName) {
        actionPanel.setPositiveButtonText(buttonName);
    }

    @Override
    public void completeSuccess() {
        ToastUtil.showForLongTime(R.string.msg_mmia_submit_tip);
        finish();
    }

    @Override
    public void showSignDialog() {
        SignatureDialog signatureDialog = new SignatureDialog();
        String signatureDialogTitle = presenter.isDraft() ? getResources().getString(R.string.msg_mmia_submit_signature) : getResources().getString(R.string.msg_approve_signature_mmia);
        signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
        signatureDialog.setDelegate(signatureDialogDelegate);

        signatureDialog.show(this.getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        @Override
        public void onSign(String sign) {
            presenter.processSign(sign);
        }
    };

    @Override
    public void showMessageNotifyDialog() {
        DialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                getString(R.string.msg_requisition_signature_message_notify_mmia),
                getString(R.string.btn_continue),
                null,
                TAG_SHOW_MESSAGE_NOTIFY_DIALOG);
        dialogFragment.show(this.getFragmentManager(), TAG_SHOW_MESSAGE_NOTIFY_DIALOG);
    }

    private boolean isTotalEqual() {
        return regimeListView.getTotal() == mmiaInfoListView.getTotal();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FOR_CUSTOM_REGIME) {
            regimeListView.addCustomRegimenItem((Regimen) data.getSerializableExtra(Constants.PARAM_CUSTOM_REGIMEN));
        }
    }
}