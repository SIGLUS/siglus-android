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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ListViewUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.AddDrugsToVIAActivity;
import org.openlmis.core.view.adapter.RequisitionFormAdapter;
import org.openlmis.core.view.adapter.RequisitionProductAdapter;
import org.openlmis.core.view.holder.RequisitionFormViewHolder;
import org.openlmis.core.view.widget.DoubleListScrollListener;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.ViaKitView;
import org.openlmis.core.view.widget.ViaKitViewForToggleOff;
import org.openlmis.core.view.widget.ViaReportConsultationNumberView;

import java.util.ArrayList;
import java.util.Date;

import roboguice.inject.InjectView;

public class VIARequisitionFragment extends BaseFragment implements VIARequisitionPresenter.VIARequisitionView, View.OnClickListener, SimpleDialogFragment.MsgDialogCallBack {
    @InjectView(R.id.requisition_form)
    ListView requisitionForm;

    @InjectView(R.id.product_name_list_view)
    ListView requisitionNameList;

    @InjectView(R.id.btn_complete)
    Button btnComplete;

    @InjectView(R.id.btn_save)
    View btnSave;

    @InjectView(R.id.view_consultation)
    ViaReportConsultationNumberView consultationView;

    @InjectView(R.id.vg_kit)
    ViaKitView kitView;

    @InjectView(R.id.action_panel)
    View actionPanel;

    @InjectView(R.id.vg_container)
    ViewGroup vgContainer;

    @InjectView(R.id.requisition_header_right)
    View bodyHeaderView;

    @InjectView(R.id.requisition_header_left)
    View productHeaderView;

    @InjectView(R.id.form_layout)
    HorizontalScrollView formLayout;

    @InjectView(R.id.tv_label_request)
    TextView headerRequestAmount;

    @InjectView(R.id.tv_label_approve)
    TextView headerApproveAmount;

    @Inject
    VIARequisitionPresenter presenter;

    protected Boolean hasDataChanged;

    private RequisitionProductAdapter requisitionProductAdapter;

    private RequisitionFormAdapter requisitionFormAdapter;
    private static final String TAG_BACK_PRESSED = "onBackPressed";
    private static final String TAG_SHOW_MESSAGE_NOTIFY_DIALOG = "showMessageNotifyDialog";
    protected View containerView;

    private long formId;

    private Date periodEndDate;
    private boolean isMissedPeriod;
    private ViaKitViewForToggleOff kitViewToggleOff;
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
    public Presenter initPresenter() {
        return presenter;
    }

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
        autoScrollLeftToRight();
    }

    private void hideMenuIfNotDraft() {
        if (!presenter.getRnrFormStatus().equals(RnRForm.STATUS.DRAFT)) {
            menu.findItem(R.id.action_add_new_drugs_to_via).setVisible(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_via_requisition, menu);
        boolean featureToggleForAddDrugsToVIA = LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_add_drugs_to_via_form);
        menu.findItem(R.id.action_add_new_drugs_to_via).setVisible(featureToggleForAddDrugsToVIA);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_new_drugs_to_via:
                startActivityForResult(AddDrugsToVIAActivity.getIntentToMe(getActivity(), presenter.getRnRForm().getPeriodBegin(), periodEndDate), Constants.REQUEST_ADD_DRUGS_TO_VIA);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void autoScrollLeftToRight() {
        if (!isHistoryForm()) {
            formLayout.post(new Runnable() {
                public void run() {
                    formLayout.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
        }
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
        requisitionProductAdapter.notifyDataSetChanged();
        requisitionFormAdapter.updateStatus(rnRForm.getStatus());

        if (rnRForm.isEmergency()) {
            refreshEmergencyRnr(rnRForm);
        } else {
            refreshNormalRnr(rnRForm);
        }
        setEditable();
        hideMenuIfNotDraft();
    }

    private void refreshNormalRnr(RnRForm rnRForm) {
        consultationView.setNormalRnrHeader();
        consultationView.setEnabled(true);
        btnSave.setVisibility(View.VISIBLE);
        setTitleWithPeriod(rnRForm);
        consultationView.setConsultationNumbers(presenter);
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
        btnSave.setVisibility(View.GONE);
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
        headerRequestAmount.setBackgroundResource(android.R.color.transparent);
        headerRequestAmount.setTextColor(getResources().getColor(R.color.color_text_primary));
        headerApproveAmount.setBackgroundResource(R.color.color_accent);
        headerApproveAmount.setTextColor(getResources().getColor(R.color.color_white));
        requisitionFormAdapter.updateStatus(RnRForm.STATUS.SUBMITTED);
    }

    @Override
    public void highLightRequestAmount() {
        headerRequestAmount.setBackgroundResource(R.color.color_accent);
        headerRequestAmount.setTextColor(getResources().getColor(R.color.color_white));
        headerApproveAmount.setBackgroundResource(android.R.color.transparent);
        headerApproveAmount.setTextColor(getResources().getColor(R.color.color_text_primary));
        requisitionFormAdapter.updateStatus(RnRForm.STATUS.DRAFT);
    }

    public void setEditable() {
        if (presenter.getRnRForm().isAuthorized()) {
            vgContainer.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            actionPanel.setVisibility(View.GONE);
        } else {
            vgContainer.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            actionPanel.setVisibility(View.VISIBLE);
        }

        if (isMissedPeriod || presenter.getRnRForm().isMissed()) {
            requisitionForm.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        } else {
            requisitionForm.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        }
    }

    private void setKitValues() {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_auto_fill_kit_rnr)) {
            kitView.removeAllViews();
            kitViewToggleOff = new ViaKitViewForToggleOff(getActivity());
            kitView.addView(kitViewToggleOff);
            kitViewToggleOff.setValue(presenter.getViaKitsViewModel());
            kitViewToggleOff.post(new Runnable() {
                @Override
                public void run() {
                    kitViewToggleOff.addTextChangeListeners(presenter.getViaKitsViewModel());
                }
            });
        } else {
            kitView.setValue(presenter.getViaKitsViewModel());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                onSaveBtnClick();
                break;
            case R.id.btn_complete:
                onProcessButtonClick();
                break;
        }
    }

    @Override
    public void showListInputError(int index) {
        final int position = index;
        requisitionForm.setSelection(position);
        requisitionForm.post(new Runnable() {
            @Override
            public void run() {
                View childAt = ListViewUtil.getViewByPosition(position, requisitionForm);
                EditText requestAmount = (EditText) childAt.findViewById(R.id.et_request_amount);
                EditText approvedAmount = (EditText) childAt.findViewById(R.id.et_approved_amount);
                if (requestAmount.isEnabled()) {
                    requestAmount.requestFocus();
                    requestAmount.setError(getString(R.string.hint_error_input));
                } else {
                    approvedAmount.requestFocus();
                    approvedAmount.setError(getString(R.string.hint_error_input));
                }
            }
        });
    }

    private void initUI() {
        initRequisitionBodyList();
        initRequisitionProductList();

        requisitionNameList.post(new Runnable() {
            @Override
            public void run() {
                productHeaderView.getLayoutParams().height = bodyHeaderView.getHeight();
            }
        });

        consultationView.initUI();
        bindListeners();
    }

    private void bindListeners() {
        requisitionForm.setOnScrollListener(new DoubleListScrollListener(requisitionForm, requisitionNameList));
        requisitionNameList.setOnScrollListener(new DoubleListScrollListener(requisitionNameList, requisitionForm));
        btnComplete.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        formLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideImm();
                return false;
            }
        });
    }

    private void initRequisitionBodyList() {
        requisitionFormAdapter = new RequisitionFormAdapter(getActivity(), presenter.getRequisitionFormItemViewModels());
        requisitionForm.setAdapter(requisitionFormAdapter);
    }

    private void initRequisitionProductList() {
        requisitionProductAdapter = new RequisitionProductAdapter(getActivity(), presenter.getRequisitionFormItemViewModels());
        requisitionNameList.setAdapter(requisitionProductAdapter);
    }

    @Override
    public void setProcessButtonName(String name) {
        btnComplete.setText(name);
    }

    @Override
    public boolean validateConsultationNumber() {
        return consultationView.validate();
    }

    //TODO when feature_auto_fill_kit_rnr removed, then you can remove this method
    @Override
    public boolean validateKitData() {
        return kitViewToggleOff.validate();
    }

    @Override
    public boolean isHistoryForm() {
        return formId != 0;
    }

    protected void onProcessButtonClick() {
        presenter.processRequisition(consultationView.getValue());
    }

    @Override
    public void showSignDialog(boolean isFormStatusDraft) {
        SignatureDialog signatureDialog = new SignatureDialog();
        String signatureDialogTitle = isFormStatusDraft ? getResources().getString(R.string.msg_via_submit_signature) : getResources().getString(R.string.msg_approve_signature_via);

        signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
        signatureDialog.setDelegate(signatureDialogDelegate);

        signatureDialog.show(getActivity().getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        @Override
        public void onCancel() {
        }

        @Override
        public void onSign(String sign) {
            presenter.processSign(sign, presenter.getRnRForm());
        }
    };

    @Override
    public void showMessageNotifyDialog() {
        SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                getString(R.string.msg_requisition_signature_message_notify_via), getString(R.string.btn_continue), null, TAG_SHOW_MESSAGE_NOTIFY_DIALOG);

        dialogFragment.show(getActivity().getFragmentManager(), TAG_SHOW_MESSAGE_NOTIFY_DIALOG);
    }

    @Override
    public void saveSuccess() {
        finish();
    }

    @Override
    public void completeSuccess() {
        ToastUtil.showForLongTime(R.string.msg_requisition_submit_tip);
        finish();
    }

    private void onSaveBtnClick() {
        presenter.saveVIAForm(consultationView.getValue());
    }

    private void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private boolean hasDataChanged() {
        if (hasDataChanged == null) {
            hasDataChanged = requisitionFormChanged() || consultationView.isHasChanged();
        }
        return hasDataChanged;
    }

    private boolean requisitionFormChanged() {
        for (int index = 0; index < requisitionForm.getChildCount(); index++) {
            Object requisitionItemTag = requisitionForm.getChildAt(index).getTag();
            if (requisitionItemTag != null
                    && requisitionItemTag instanceof RequisitionFormViewHolder
                    && ((RequisitionFormViewHolder) requisitionItemTag).isHasDataChanged()) {
                return true;
            }
        }
        return false;
    }

    public void onBackPressed() {
        if (getResources().getBoolean(R.bool.feature_show_pop_up_even_no_data_changed_418)) {
            if (presenter.getRnrFormStatus() == RnRForm.STATUS.DRAFT) {
                hasDataChanged = true;
            }
        }

        if (hasDataChanged()) {
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                    getString(R.string.msg_mmia_onback_confirm), getString(R.string.btn_positive), getString(R.string.btn_negative), TAG_BACK_PRESSED);
            dialogFragment.show(getActivity().getFragmentManager(), "back_confirm_dialog");
            dialogFragment.setCallBackListener(this);
        } else {
            finish();
        }
    }

    private void removeTempForm() {
        if (!isHistoryForm()) {
            presenter.removeRequisition();
            presenter.removeAllNewRnrItems();
        }
    }

    @Override
    public void positiveClick(String tag) {
        if (tag.equals(TAG_BACK_PRESSED)) {
            removeTempForm();
            finish();
        }
    }

    @Override
    public void negativeClick(String tag) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_ADD_DRUGS_TO_VIA) {
            if (resultCode == Activity.RESULT_OK) {
                loadData();
            }
        }
    }
}
