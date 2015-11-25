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

package org.openlmis.core.view.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.openlmis.core.view.widget.MMIAInfoList;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARnrForm;
import org.openlmis.core.view.widget.SignatureDialog;

import java.util.ArrayList;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_mmia)
public class MMIARequisitionActivity extends BaseActivity implements MMIARequisitionPresenter.MMIARequisitionView, View.OnClickListener, SimpleDialogFragment.MsgDialogCallBack {

    @InjectView(R.id.rnr_form_list)
    protected MMIARnrForm rnrFormList;

    @InjectView(R.id.regime_list)
    protected MMIARegimeList regimeListView;

    @InjectView(R.id.mmia_info_list)
    protected MMIAInfoList mmiaInfoListView;

    @InjectView(R.id.btn_complete)
    protected Button btnComplete;

    @InjectView(R.id.tv_regime_total)
    protected TextView tvRegimeTotal;

    @InjectView(R.id.et_comment)
    private TextView etComment;

    @InjectView(R.id.scrollview)
    private ScrollView scrollView;

    @InjectView(R.id.btn_save)
    protected View btnSave;

    @InjectView(R.id.tv_total_mismatch)
    protected TextView tvMismatch;

    @InjectPresenter(MMIARequisitionPresenter.class)
    MMIARequisitionPresenter presenter;

    private Boolean hasDataChanged;
    private boolean commentHasChanged = false;
    private boolean isHistoryForm;
    private long formId;

    protected static final String TAG_BACK_PRESSED = "onBackPressed";
    private static final String TAG_MISMATCH = "mismatch";
    private static final String TAG_SHOW_MESSAGE_NOTIFY_DIALOG = "showMessageNotifyDialog";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasDataChanged = (Boolean) dataFragment.getData(Constants.KEY_HAS_DATA_CHANGED);
        formId = getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
        isHistoryForm = formId != 0;

        scrollView.setVisibility(View.INVISIBLE);

        presenter.loadData(formId);
    }

    @Override
    public void initView(final RnRForm form) {
        scrollView.setVisibility(View.VISIBLE);
        rnrFormList.initView(new ArrayList<>(form.getRnrFormItemListWrapper()));
        regimeListView.initView(form.getRegimenItemListWrapper(), tvRegimeTotal);
        mmiaInfoListView.initView(form.getBaseInfoItemListWrapper());

        presenter.setBtnCompleteText();

        if (!isHistoryForm) {
            scrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            btnSave.setVisibility(View.VISIBLE);
            btnComplete.setVisibility(View.VISIBLE);
        } else {
            scrollView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            btnSave.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);
        }

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.add_header_info_reduce_header_size_348)) {
            setTitle(getString(R.string.label_mmia_title,
                    DateUtil.formatDateWithoutYear(form.getPeriodBegin()),
                    DateUtil.formatDateWithoutYear(form.getPeriodEnd())));
        } else {
            setTitle(isHistoryForm ? new RnRFormViewModel(form).getPeriod() : getString(R.string.title_mmia_spread));
        }

        highlightTotalDifference();

        etComment.setText(form.getComments());

        bindListeners();
    }

    @Override
    public void setProcessButtonName(String name){
        btnComplete.setText(name);
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

        btnSave.setOnClickListener(this);

        btnComplete.setOnClickListener(this);

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                clearEditErrorFocus();
                hideImm();
                return false;
            }
        });
    }

    private void clearEditErrorFocus() {
        scrollView.requestFocus();
    }

    TextWatcher commentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            commentHasChanged = true;
            try {
                presenter.getRnrForm(formId).setComments(s.toString());
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }
    };

    TextWatcher totalTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            highlightTotalDifference();
        }
    };

    private void highlightTotalDifference() {
        if (isTotalEqual()) {
            regimeListView.deHighLightTotal();
            mmiaInfoListView.deHighLightTotal();
            tvMismatch.setVisibility(View.INVISIBLE);
        } else {
            regimeListView.highLightTotal();
            mmiaInfoListView.highLightTotal();
            tvMismatch.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {

        if (getResources().getBoolean(R.bool.feature_show_pop_up_even_no_data_changed_418)){
            if(presenter.getRnrFormStatus() == RnRForm.STATUS.DRAFT) {
                hasDataChanged = true;
            }
        }

        if (hasDataChanged()) {
            DialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                    null,
                    getString(R.string.msg_mmia_onback_confirm),
                    getString(R.string.btn_positive),
                    getString(R.string.btn_negative),
                    TAG_BACK_PRESSED);
            dialogFragment.show(getFragmentManager(), "");
        } else {
            super.onBackPressed();
        }
    }

    private void removeTempForm() {
        if (!isHistoryForm) {
            presenter.removeRnrForm();
        }
    }

    private boolean hasDataChanged() {
        if (hasDataChanged == null) {
            hasDataChanged = regimeListView.hasDataChanged() || mmiaInfoListView.hasDataChanged() || commentHasChanged;
        }
        return hasDataChanged;
    }

    private void backHomePage() {
        finish();
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
    public void showErrorMessage(String msg) {
        ToastUtil.show(msg);
    }

    @Override
    protected void onDestroy() {
        if (hasDataChanged()) {
            dataFragment.putData(Constants.KEY_HAS_DATA_CHANGED, hasDataChanged());
        }
        super.onDestroy();
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
            default:
                break;

        }
    }

    private void onProcessButtonClick() {
        if (regimeListView.isCompleted() && mmiaInfoListView.isCompleted()) {
            presenter.processRequisition(regimeListView.getDataList(), mmiaInfoListView.getDataList(), etComment.getText().toString());
        }
    }

    @Override
    public void completeSuccess() {
        ToastUtil.showForLongTime(R.string.msg_mmia_submit_tip);
        backHomePage();
    }

    @Override
    public void saveSuccess() {
        backHomePage();
    }

    @Override
    public void showSignDialog(boolean isFormStatusDraft) {
        SignatureDialog signatureDialog = new SignatureDialog();
        String signatureDialogTitle = isFormStatusDraft ? getResources().getString(R.string.msg_mmia_submit_signature) : getResources().getString(R.string.msg_mmia_approve_signature);

        signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
        signatureDialog.setDelegate(signatureDialogDelegate);

        signatureDialog.show(this.getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        @Override
        public void onCancel() {
        }

        @Override
        public void onSign(String sign) {
            presenter.processSign(sign);
        }
    };

    @Override
    public void showMessageNotifyDialog() {
        DialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                getString(R.string.msg_requisition_signature_message_notify),
                getString(R.string.btn_continue),
                null,
                TAG_SHOW_MESSAGE_NOTIFY_DIALOG);
        dialogFragment.show(this.getFragmentManager(), TAG_SHOW_MESSAGE_NOTIFY_DIALOG);
    }

    private boolean isTotalEqual() {
        return regimeListView.getTotal() == mmiaInfoListView.getTotal();
    }

    private void onSaveBtnClick() {
        presenter.saveDraftForm(regimeListView.getDataList(), mmiaInfoListView.getDataList(), etComment.getText().toString());
    }

    public static Intent getIntentToMe(Context context, long formId) {
        Intent intent = new Intent(context, MMIARequisitionActivity.class);
        intent.putExtra(Constants.PARAM_FORM_ID, formId);
        return intent;
    }

    @Override
    public void positiveClick(String tag) {
        if (tag.equals(TAG_BACK_PRESSED)){
            removeTempForm();
            finish();
        }
    }

    @Override
    public void negativeClick(String tag) {
    }
}
