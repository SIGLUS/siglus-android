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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.presenter.VIARequisitionView;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.AddDrugsToVIAActivity;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.widget.ActionPanelView;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.openlmis.core.view.widget.ViaKitView;
import org.openlmis.core.view.widget.ViaReportConsultationNumberView;
import org.openlmis.core.view.widget.ViaRequisitionBodyView;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

import static org.openlmis.core.utils.Constants.REQUEST_ADD_DRUGS_TO_VIA;

public class VIARequisitionFragment extends BaseReportFragment implements VIARequisitionView {
    @InjectView(R.id.view_consultation)
    ViaReportConsultationNumberView consultationView;

    @InjectView(R.id.vg_kit)
    ViaKitView kitView;

    @InjectView(R.id.view_via_body)
    ViaRequisitionBodyView bodyView;

    @InjectView(R.id.vg_container)
    ViewGroup vgContainer;

    @InjectView(R.id.action_panel)
    ActionPanelView actionPanel;

    @Inject
    VIARequisitionPresenter presenter;

    private static final String TAG_BACK_PRESSED = "onBackPressed";
    private static final String TAG_SHOW_MESSAGE_NOTIFY_DIALOG = "showMessageNotifyDialog";
    protected View containerView;

    private long formId;

    private Date periodEndDate;
    private boolean isMissedPeriod;
    private ArrayList<StockCard> emergencyStockCards;

    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
        periodEndDate = ((Date) getActivity().getIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
        isMissedPeriod = getActivity().getIntent().getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false);
        emergencyStockCards = (ArrayList<StockCard>) getActivity().getIntent().getSerializableExtra(Constants.PARAM_SELECTED_EMERGENCY);
    }

    @Override
    protected BaseReportPresenter injectPresenter() {
        presenter = RoboGuice.getInjector(getActivity()).getInstance(VIARequisitionPresenter.class);
        return presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        containerView = inflater.inflate(R.layout.fragment_via_requisition, container, false);
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
            loadData();
        }
        bodyView.autoScrollLeftToRight();
    }

    public void hideOrShowAddProductMenuInVIAPage() {
        menu.findItem(R.id.action_add_new_drugs_to_via).setVisible(presenter.isFormProductEditable());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_via_requisition, menu);
        this.menu = menu;
        hideOrShowAddProductMenuInVIAPage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_new_drugs_to_via) {
            ArrayList<String> productCodes = new ArrayList<>(FluentIterable.from(presenter.getRequisitionFormItemViewModels()).transform(new Function<RequisitionFormItemViewModel, String>() {
                @Override
                public String apply(RequisitionFormItemViewModel requisitionFormItemViewModel) {
                    return requisitionFormItemViewModel.getFmn();
                }
            }).toList());

            startActivityForResult(AddDrugsToVIAActivity.getIntentToMe(getActivity(), presenter.getRnRForm().getPeriodBegin(), productCodes), REQUEST_ADD_DRUGS_TO_VIA);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        if (isFromSelectEmergencyPage()) {
            presenter.loadEmergencyData(emergencyStockCards, new Date(LMISApp.getInstance().getCurrentTimeMillis()));
        } else {
            presenter.loadData(formId, periodEndDate);
        }
    }

    private boolean isFromSelectEmergencyPage() {
        return emergencyStockCards != null;
    }

    @Override
    public void refreshRequisitionForm(RnRForm rnRForm) {
        bodyView.refresh(rnRForm);

        if (rnRForm.isEmergency()) {
            refreshEmergencyRnr(rnRForm);
        } else {
            refreshNormalRnr(rnRForm);
        }
        setEditable();
        hideOrShowAddProductMenuInVIAPage();
    }

    private void refreshNormalRnr(RnRForm rnRForm) {
        consultationView.refreshNormalRnrConsultationView(presenter);
        actionPanel.setNegativeButtonVisibility(View.VISIBLE);
        setTitleWithPeriod(rnRForm);
        setKitValues();
    }

    private void refreshEmergencyRnr(RnRForm rnRForm) {
        if (!rnRForm.isAuthorized()) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtil.showForLongTime(R.string.msg_emergency_requisition_cant_edit);
                }
            };
            consultationView.setEditClickListener(onClickListener);
            kitView.setEditClickListener(onClickListener);
        }

        kitView.setEmergencyKitValues();
        consultationView.setEmergencyRnrHeader();

        getActivity().setTitle(getString(R.string.label_emergency_requisition_title,
                DateUtil.formatDateWithoutYear(new Date(LMISApp.getInstance().getCurrentTimeMillis()))));
        actionPanel.setNegativeButtonVisibility(View.GONE);
    }

    public void setTitleWithPeriod(RnRForm rnRForm) {
        if (rnRForm != null) {
            getActivity().setTitle(getString(R.string.label_requisition_title,
                    DateUtil.formatDateWithoutYear(rnRForm.getPeriodBegin()),
                    DateUtil.formatDateWithoutYear(rnRForm.getPeriodEnd())));
        } else {
            getActivity().setTitle(getString(R.string.title_requisition));
        }
    }

    @Override
    public void highLightApprovedAmount() {
        bodyView.highLightApprovedAmount();
    }

    @Override
    public void highLightRequestAmount() {
        bodyView.highLightRequestAmount();
    }

    public void setEditable() {
        if (presenter.getRnRForm().isAuthorized()) {
            vgContainer.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            actionPanel.setVisibility(View.GONE);
        } else {
            vgContainer.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            actionPanel.setVisibility(View.VISIBLE);
        }
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            bodyView.setEditable(false);
        } else {
            bodyView.setEditable(isMissedPeriod || presenter.getRnRForm().isMissed());
        }
    }

    private void setKitValues() {
        kitView.setValue(presenter.getViaKitsViewModel());
    }

    @Override
    public void showListInputError(int index) {
        bodyView.showListInputError(index);
    }

    private void initUI() {
        bodyView.initUI(presenter);
        consultationView.initUI();
        bindListeners();
    }

    private void bindListeners() {
        actionPanel.setListener(getOnCompleteClickListener(), getOnSaveClickListener());
        bodyView.setHideImmOnTouchListener();
    }

    @NonNull
    private SingleClickButtonListener getOnCompleteClickListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                if (presenter.processRequisition(consultationView.getValue())) {
                    showSignDialog();
                }
            }
        };
    }

    @NonNull
    private SingleClickButtonListener getOnSaveClickListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                loading();
                Subscription subscription = presenter.getSaveFormObservable(consultationView.getValue()).subscribe(getOnSavedSubscriber());
                subscriptions.add(subscription);
            }
        };
    }

    @NonNull
    public Subscriber<RnRForm> getOnSavedSubscriber() {
        return new Subscriber<RnRForm>() {
            @Override
            public void onCompleted() {
                loaded();
                finish();
            }

            @Override
            public void onError(Throwable e) {
                loaded();
                ToastUtil.show(getString(R.string.hint_save_requisition_failed));
            }

            @Override
            public void onNext(RnRForm rnRForm) {
            }
        };
    }

    @Override
    public void setProcessButtonName(String buttonName) {
        actionPanel.setPositiveButtonText(buttonName);
    }

    @Override
    public boolean validateConsultationNumber() {
        return consultationView.validate();
    }

    @Override
    public void showSignDialog() {
        SignatureDialog signatureDialog = new SignatureDialog();
        String signatureDialogTitle = presenter.isDraft() ? getResources().getString(R.string.msg_via_submit_signature) : getResources().getString(R.string.msg_approve_signature_via);

        signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
        signatureDialog.setDelegate(signatureDialogDelegate);

        signatureDialog.show(getActivity().getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        public void onSign(String sign) {
            presenter.processSign(sign);
        }
    };

    @Override
    public void showMessageNotifyDialog() {
        SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                getString(R.string.msg_requisition_signature_message_notify_via), getString(R.string.btn_continue), null, TAG_SHOW_MESSAGE_NOTIFY_DIALOG);

        dialogFragment.show(getActivity().getFragmentManager(), TAG_SHOW_MESSAGE_NOTIFY_DIALOG);
    }

    @Override
    public void completeSuccess() {
        ToastUtil.showForLongTime(R.string.msg_requisition_submit_tip);
        finish();
    }

    @Override
    protected void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    public void onBackPressed() {
        if (presenter.getRnrFormStatus() == RnRForm.STATUS.DRAFT) {
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                    getString(R.string.msg_back_confirm), getString(R.string.btn_positive), getString(R.string.btn_negative), TAG_BACK_PRESSED);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_ADD_DRUGS_TO_VIA && resultCode == Activity.RESULT_OK) {
            Date periodBegin = (Date) data.getSerializableExtra(Constants.PARAM_PERIOD_BEGIN);

            List<RnrFormItem> drugInVIAs = (ArrayList<RnrFormItem>) data.getExtras().get(Constants.PARAM_ADDED_DRUGS_TO_VIA);
            presenter.populateAdditionalDrugsViewModels(drugInVIAs, periodBegin);
            bodyView.refreshProductNameList();
        }
    }
}