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

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.adapter.RequisitionFormAdapter;
import org.openlmis.core.view.adapter.RequisitionProductAdapter;
import org.openlmis.core.view.holder.RequisitionFormViewHolder;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;
import org.openlmis.core.view.widget.SignatureDialog;

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

    @InjectView(R.id.action_panel)
    View actionPanel;

    @InjectView(R.id.vg_container)
    ViewGroup vgContainer;

    @InjectView(R.id.edit_text)
    EditText etConsultationNumbers;

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
    private boolean consultationNumbersHasChanged;
    protected boolean isHistoryForm;
    private static final String TAG_BACK_PRESSED = "onBackPressed";
    private static final String TAG_SHOW_MESSAGE_NOTIFY_DIALOG = "showMessageNotifyDialog";
    protected View containerView;

    private long formId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            presenter.attachView(this);
        } catch (ViewNotMatchException e) {
            e.reportToFabric();
        }

        formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
        isHistoryForm = formId != 0;
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
        initUI();

        if (isSavedInstanceState) {
            refreshRequisitionForm(presenter.getRnRForm());
        }else {
            presenter.loadData(formId);
        }
    }

    @Override
    public void refreshRequisitionForm(RnRForm rnRForm) {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.add_header_info_reduce_header_size_348)) {
            setTitleWithPeriodWithToggle(rnRForm);
        } else {
            setTitleWithPeriodWithoutToggle(rnRForm);
        }
        requisitionProductAdapter.notifyDataSetChanged();
        requisitionFormAdapter.updateStatus(rnRForm.getStatus());
        setConsultationNumbers();
        setEditable();
    }

    public void setTitleWithPeriodWithToggle(RnRForm rnRForm) {
        if (rnRForm != null) {
            getActivity().setTitle(getString(R.string.label_requisition_title,
                    DateUtil.formatDateWithoutYear(rnRForm.getPeriodBegin()),
                    DateUtil.formatDateWithoutYear(rnRForm.getPeriodEnd())));
        } else {
            getActivity().setTitle(getString(R.string.title_requisition));
        }
    }

    public void setTitleWithPeriodWithoutToggle(RnRForm rnRForm) {
        if (isHistoryForm) {
            getActivity().setTitle(new RnRFormViewModel(rnRForm).getPeriod());
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
    }

    private void setConsultationNumbers() {
        etConsultationNumbers.setText(presenter.getConsultationNumbers());
        etConsultationNumbers.post(new Runnable() {
            @Override
            public void run() {
                etConsultationNumbers.addTextChangedListener(etConsultationNumbersTextWatcher);
            }
        });
    }

    @Override
    public void showErrorMessage(String msg) {
        ToastUtil.show(msg);
    }

    @Override
    public void showSaveErrorMessage() {
        ToastUtil.show(getString(R.string.hint_save_mmia_failed));
    }

    @Override
    public void showCompleteErrorMessage() {
        ToastUtil.show(getString(R.string.hint_requisition_complete_failed));
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
                View childAt = getViewByPosition(position, requisitionForm);
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

        etConsultationNumbers.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});

        bindListeners();
    }

    private void bindListeners() {
        requisitionForm.setOnScrollListener(new MyScrollListener(requisitionForm, requisitionNameList));
        requisitionNameList.setOnScrollListener(new MyScrollListener(requisitionNameList, requisitionForm));
        btnComplete.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        etConsultationNumbers.post(new Runnable() {
            @Override
            public void run() {
                etConsultationNumbers.addTextChangedListener(etConsultationNumbersTextWatcher);
            }
        });

        formLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((BaseActivity) getActivity()).hideImm();
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

    protected void onProcessButtonClick() {
        String consultationNumbers = etConsultationNumbers.getText().toString();
        if (TextUtils.isEmpty(consultationNumbers)) {
            etConsultationNumbers.setError(getString(R.string.hint_error_input));
            return;
        }
        presenter.processRequisition(consultationNumbers);
    }

    @Override
    public void showSignDialog(boolean isFormStatusDraft) {
        SignatureDialog signatureDialog = new SignatureDialog();
        String signatureDialogTitle = isFormStatusDraft ? getResources().getString(R.string.msg_via_submit_signature) : getResources().getString(R.string.msg_approve_signature);

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
                getString(R.string.msg_requisition_signature_message_notify),
                getString(R.string.btn_continue),
                null,
                TAG_SHOW_MESSAGE_NOTIFY_DIALOG);

        dialogFragment.show(getActivity().getFragmentManager(), TAG_SHOW_MESSAGE_NOTIFY_DIALOG);
    }

    @Override
    public void saveSuccess() {
        backToHomePage();
    }

    @Override
    public void completeSuccess() {
        ToastUtil.showForLongTime(R.string.msg_requisition_submit_tip);
        backToHomePage();
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private void onSaveBtnClick() {
        presenter.saveVIAForm(etConsultationNumbers.getText().toString());
    }

    @Override
    public void loading() {
        ((BaseActivity) getActivity()).loading();
    }

    @Override
    public void loading(String message) {
        ((BaseActivity) getActivity()).loading(message);
    }

    @Override
    public void loaded() {
        ((BaseActivity) getActivity()).loaded();
    }


    private class MyScrollListener implements AbsListView.OnScrollListener {

        ListView list1;
        ListView list2;

        public MyScrollListener(ListView list1, ListView list2) {
            this.list1 = list1;
            this.list2 = list2;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == 0 || scrollState == 1) {
                View subView1 = view.getChildAt(0);

                if (subView1 != null) {
                    final int top1 = subView1.getTop();
                    View subview2 = list2.getChildAt(0);
                    if (subview2 != null) {
                        int top2 = subview2.getTop();
                        int position = view.getFirstVisiblePosition();

                        if (top1 != top2) {
                            list2.setSelectionFromTop(position, top1);
                        }
                    }
                }
            }
        }

        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            View subView1 = view.getChildAt(0);
            if (subView1 != null) {
                int top1 = subView1.getTop();

                View subView2 = list2.getChildAt(0);
                if (subView2 != null) {
                    int top2 = list2.getChildAt(0).getTop();
                    if (top1 != top2) {
                        list1.setSelectionFromTop(firstVisibleItem, top1);
                        list2.setSelectionFromTop(firstVisibleItem, top1);
                    }
                }
            }
        }
    }


    public void backToHomePage() {
        getActivity().finish();
    }

    private boolean hasDataChanged() {
        if (hasDataChanged == null) {
            hasDataChanged = requisitionFormChanged() || consultationNumbersHasChanged;
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

    TextWatcher etConsultationNumbersTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String input = etConsultationNumbers.getText().toString();
            if (!input.equals(presenter.getConsultationNumbers())) {
                consultationNumbersHasChanged = true;
                presenter.setConsultationNumbers(input);
            }
        }
    };

    public void onBackPressed() {
        if (getResources().getBoolean(R.bool.feature_show_pop_up_even_no_data_changed_418)) {
            if (presenter.getRnrFormStatus() == RnRForm.STATUS.DRAFT) {
                hasDataChanged = true;
            }
        }

        if (hasDataChanged()) {
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                    getString(R.string.msg_mmia_onback_confirm),
                    getString(R.string.btn_positive),
                    getString(R.string.btn_negative),
                    TAG_BACK_PRESSED);
            dialogFragment.show(getActivity().getFragmentManager(), "back_confirm_dialog");
            dialogFragment.setCallBackListener(this);
        } else {
            getActivity().finish();
        }
    }

    private void removeTempForm() {
        if (!isHistoryForm) {
            try {
                presenter.removeRequisition();
            } catch (LMISException e) {
                ToastUtil.show("Delete Failed");
                e.reportToFabric();
            }
        }
    }

    @Override
    public void positiveClick(String tag) {
        if (tag.equals(TAG_BACK_PRESSED)) {
            removeTempForm();
            getActivity().finish();
        }
    }

    @Override
    public void negativeClick(String tag) {
    }

}
