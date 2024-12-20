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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.DialogFragment;
import java.util.Date;
import java.util.List;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openlmis.core.R;
import org.openlmis.core.annotation.BindEventBus;
import org.openlmis.core.event.DebugMMIARequisitionEvent;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.ViewUtil;
import org.openlmis.core.view.widget.MMIADispensedInfoList;
import org.openlmis.core.view.widget.MMIAPatientInfoList;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARegimeListWrap;
import org.openlmis.core.view.widget.MMIARegimeThreeLineList;
import org.openlmis.core.view.widget.MMIARnrFormProductList;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

@BindEventBus
public class MMIARequisitionFragment extends BaseReportFragment implements
    MMIARequisitionPresenter.MMIARequisitionView {

  @InjectView(R.id.rnr_form_list)
  protected MMIARnrFormProductList rnrFormList;

  @InjectView(R.id.mmia_regime_three_line_list)
  protected MMIARegimeThreeLineList mmiaRegimeThreeLineListView;
  @InjectView(R.id.mmia_therapeutic_layout)
  protected LinearLayout mmiaThreaPeuticLayout;

  @InjectView(R.id.regime_list_wrap)
  protected MMIARegimeListWrap regimeWrap;

  @InjectView(R.id.mmia_patient_info_list)
  protected MMIAPatientInfoList mmiaPatientInfoListView;
  @InjectView(R.id.mmia_requisition_dispensed_info)
  protected MMIADispensedInfoList mmiaDispensedInfoList;

  @InjectView(R.id.tv_regime_total)
  protected TextView tvRegimeTotal;
  @InjectView(R.id.tv_regime_total_pharmacy)
  protected TextView tvRegimeTotalPharmacy;

  @InjectView(R.id.tv_total_pharmacy_title)
  protected TextView tvTotalPharmacyTitle;

  @InjectView(R.id.tv__total_patients_title)
  protected TextView tvTotalPatientsTitle;

  @InjectView(R.id.mmia_regime_three_line_total)
  protected TextView mmiaRegimeThreeLineTotal;
  @InjectView(R.id.mmia_regime_three_line_pharmacy)
  protected TextView mmiaRegimeThreeLinePharmacy;
  @InjectView(R.id.type_dispensed_total_within)
  protected TextView mmiaTotalDispensedWithMonth;

  @InjectView(R.id.et_total_patient)
  protected EditText etTotalPatient;

  @InjectView(R.id.et_total_month)
  protected EditText etTotalMonth;

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

  MMIARequisitionPresenter presenter;

  private long formId;
  protected View containerView;
  private Date periodEndDate;

  private static final String TAG_MISMATCH = "mismatch";
  public static final int REQUEST_FOR_CUSTOM_REGIME = 100;

  protected int actionBarHeight;

  private final ActivityResultCallback<ActivityResult> addRegimenProductCallback = result -> {
    if (result.getResultCode() == Activity.RESULT_OK) {
      Intent data = result.getData();
      regimeWrap.addCustomRegimenItem((Regimen) data.getSerializableExtra(Constants.PARAM_CUSTOM_REGIMEN));
    }
  };

  private final ActivityResultLauncher<Intent> addRegimenProductLauncher = registerForActivityResult(
      new StartActivityForResult(), addRegimenProductCallback);

  public ActivityResultCallback<ActivityResult> getAddRegimenProductCallback() {
    return addRegimenProductCallback;
  }

  public ActivityResultLauncher<Intent> getAddRegimenProductLauncher() {
    return addRegimenProductLauncher;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    formId = requireActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
    periodEndDate = ((Date) requireActivity().getIntent()
        .getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
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

  @Override
  public void onDestroyView() {
    rnrFormList.removeListenerOnDestroyView();
    super.onDestroyView();
  }

  @Override
  protected BaseReportPresenter injectPresenter() {
    presenter = RoboGuice.getInjector(requireActivity()).getInstance(MMIARequisitionPresenter.class);
    return presenter;
  }

  protected void initUI() {
    scrollView.setVisibility(View.INVISIBLE);
    setEditable(isHistoryForm());
    disableFreezeHeaderScroll();
    initActionBarHeight();
    setRegimenListener();
  }

  private void setEditable(boolean disableEdit) {
    if (disableEdit) {
      scrollView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
      actionPanelView.setVisibility(View.GONE);
      etComment.setEnabled(false);
    } else {
      scrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
      actionPanelView.setVisibility(View.VISIBLE);
      etComment.setEnabled(true);
    }
  }

  private boolean isHistoryForm() {
    return formId != 0;
  }

  private void setRegimenListener() {
    regimeWrap.setRegimeListener(new MMIARegimeList.MMIARegimeListener() {
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
    rnrItemsHeaderFreezeRight.setOnTouchListener((v, event) -> true);
  }

  @Override
  public void refreshRequisitionForm(RnRForm form) {
    scrollView.setVisibility(View.VISIBLE);
    List<RegimenItemThreeLines> regimeTypes = form.getRegimenThreeLineListWrapper();
    rnrFormList.initView(form.getRnrFormItemListWrapper(),
        !(regimeTypes != null && !regimeTypes.isEmpty()));
    if (regimeTypes != null && !regimeTypes.isEmpty()) {
      mmiaRegimeThreeLineListView
          .initView(mmiaRegimeThreeLineTotal, mmiaRegimeThreeLinePharmacy, regimeTypes);
      mmiaDispensedInfoList.initView(form.getBaseInfoItemListWrapper(), presenter);
    } else {
      mmiaThreaPeuticLayout.setVisibility(View.GONE);
      tvRegimeTotalPharmacy.setVisibility(View.GONE);
      tvTotalPharmacyTitle.setVisibility(View.GONE);
      tvTotalPatientsTitle.setText(R.string.total_title_old);
      mmiaDispensedInfoList.setVisibility(View.GONE);
    }
    regimeWrap.initView(tvRegimeTotal, tvRegimeTotalPharmacy, tvTotalPharmacyTitle, presenter);
    mmiaPatientInfoListView.initView(form.getBaseInfoItemListWrapper());
    inflateFreezeHeaderView();
    getActivity().setTitle(
        getString(R.string.label_mmia_title, DateUtil.formatDateWithoutYear(form.getPeriodBegin()),
            DateUtil.formatDateWithoutYear(form.getPeriodEnd())));
    etComment.setText(form.getComments());
    etTotalPatient.setText(form.getTotalValueItemByName(getString(R.string.table_total_patient_key)));
    etTotalMonth.setText(form.getTotalValueItemByName(getString(R.string.table_total_month_key)));
    highlightTotalDifference();
    bindListeners();
    setEditable(form.isAuthorizedOrInApprovalOrApproved());
  }

  private void inflateFreezeHeaderView() {
    View leftHeaderView = rnrFormList.getLeftHeaderView();
    rnrItemsHeaderFreezeLeft.removeAllViews();
    rnrItemsHeaderFreezeLeft.addView(leftHeaderView);

    ViewGroup rightHeaderView = rnrFormList.getRightHeaderView();
    rnrItemsHeaderFreezeRight.removeAllViews();
    rnrItemsHeaderFreezeRight.addView(rightHeaderView);

    rnrFormList.post(() -> {
      ViewUtil.syncViewHeight(leftHeaderView, rightHeaderView);
      rnrFormList.setMarginForFreezeHeader();
    });
  }


  protected void bindListeners() {
    etComment.addTextChangedListener(commentTextWatcher);
    actionPanelView.setListener(getOnCompleteListener(), getOnSaveListener());
    scrollView.setOnTouchListener((v, event) -> {
      scrollView.requestFocus();
      return false;
    });

    bindFreezeHeaderListener();
  }

  @NonNull
  private SingleClickButtonListener getOnSaveListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        loading();
        Subscription subscription = presenter
            .getSaveFormObservable(rnrFormList.getItemFormList(), regimeWrap.getDataList(),
                setTotalItemValues(mmiaPatientInfoListView.getDataList()),
                mmiaRegimeThreeLineListView.getDataList(), etComment.getText().toString())
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
        // do nothing
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getOnCompleteListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        if (rnrFormList.isCompleted()
            && regimeWrap.isCompleted()
            && mmiaPatientInfoListView.isCompleted()
            && mmiaRegimeThreeLineListView.isCompleted()
            && mmiaDispensedInfoList.isCompleted()) {
          presenter.setViewModels(rnrFormList.getItemFormList(), regimeWrap.getDataList(),
              setTotalItemValues(mmiaPatientInfoListView.getDataList()),
              mmiaRegimeThreeLineListView.getDataList(), etComment.getText().toString());
          if (presenter.viewModelHasNull()) {
            ToastUtil.show(R.string.msg_requisition_field_exist_null);
          } else if (!presenter.validateFormPeriod()) {
            ToastUtil.show(R.string.msg_requisition_not_unique);
          } else if (editTextContentIsEmpty(etTotalPatient)) {
            etTotalPatient.setError(getString(R.string.hint_error_input));
            etTotalPatient.requestFocus();
          } else if (editTextContentIsEmpty(etTotalMonth)) {
            etTotalMonth.setError(getString(R.string.hint_error_input));
            etTotalMonth.requestFocus();
          } else if (shouldCommentMandatory()) {
            etComment.setError(getString(R.string.mmia_comment_should_not_empty));
            etComment.requestFocus();
          } else {
            showSignDialog();
          }
        }
      }
    };
  }

  private boolean editTextContentIsEmpty(EditText editText) {
    return TextUtils.isEmpty(editText.getText().toString());
  }

  private List<BaseInfoItem> setTotalItemValues(List<BaseInfoItem> patients) {
    for (BaseInfoItem baseInfoItem : patients) {
      if (getString(R.string.table_total_patient_key).equals(baseInfoItem.getName())) {
        baseInfoItem.setValue(etTotalPatient.getText().toString());
      }
      if (getString(R.string.table_total_month_key).equals(baseInfoItem.getName())) {
        baseInfoItem.setValue(etTotalMonth.getText().toString());
      }
    }
    return patients;
  }

  private boolean shouldCommentMandatory() {
    long totalRegimes =
        getLongFromTextView(tvRegimeTotal) + getLongFromTextView(tvRegimeTotalPharmacy);
    long totalLines = getLongFromTextView(mmiaRegimeThreeLineTotal) + getLongFromTextView(
        mmiaRegimeThreeLinePharmacy);
    long totalWithinMonths = getLongFromTextView(mmiaTotalDispensedWithMonth);

    boolean isCommentEmpty = TextUtils.isEmpty(etComment.getText().toString());
    return isCommentEmpty && (totalRegimes != totalLines || totalLines != totalWithinMonths);
  }

  private long getLongFromTextView(TextView textView) {
    return Long.parseLong(textView.getText().toString());
  }

  private void bindFreezeHeaderListener() {
    ViewTreeObserver verticalViewTreeObserver = scrollView.getViewTreeObserver();
    verticalViewTreeObserver.addOnScrollChangedListener(this::hideOrDisplayRnrItemsHeader);

    rnrFormList.getRnrItemsHorizontalScrollView().setOnScrollChangedListener((l, t, oldl, oldt)
        -> rnrItemsHeaderFreezeRight.scrollBy(l - oldl, 0));
  }

  private void initActionBarHeight() {
    containerView.post(() -> {
      int[] initialTopLocationOfRnrForm = new int[2];
      containerView.getLocationOnScreen(initialTopLocationOfRnrForm);
      actionBarHeight = initialTopLocationOfRnrForm[1];
    });
  }

  protected void hideOrDisplayRnrItemsHeader() {
    rnrItemsHeaderFreeze.setVisibility(isNeedHideFreezeHeader() ? View.INVISIBLE : View.VISIBLE);
  }

  private boolean isNeedHideFreezeHeader() {
    int[] rnrItemsViewLocation = new int[2];
    rnrFormList.getLocationOnScreen(rnrItemsViewLocation);
    final int rnrFormY = rnrItemsViewLocation[1];

    int lastItemHeight = rnrFormList.getRightViewGroup()
        .getChildAt(rnrFormList.getRightViewGroup().getChildCount() - 1).getHeight();

    final int offsetY = -rnrFormY + rnrItemsHeaderFreeze.getHeight() + actionBarHeight;

    final int hiddenThresholdY = rnrFormList.getHeight() - lastItemHeight;

    return offsetY > hiddenThresholdY;
  }

  TextWatcher commentTextWatcher = new SimpleTextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
      presenter.setComments(s.toString());
    }
  };

  private void highlightTotalDifference() {
    regimeWrap.deHighLightTotal();
    if (mmiaThreaPeuticLayout.getVisibility() != View.GONE) {
      mmiaRegimeThreeLineListView.deHighLightTotal();
    }
    tvMismatch.setVisibility(View.INVISIBLE);
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
    dialogFragment.show(getParentFragmentManager(), "not_match_dialog");
  }

  @Override
  public void setProcessButtonName(String buttonName) {
    actionPanelView.setPositiveButtonText(buttonName);
  }

  @Override
  public void completeSuccess() {
    ToastUtil.show(R.string.msg_mmia_submit_tip);
    finish();
  }

  @NonNull
  public String getSignatureDialogTitle() {
    return presenter.isDraftOrDraftMissed() ? getResources()
        .getString(R.string.msg_mmia_submit_signature)
        : getResources().getString(R.string.msg_approve_signature_mmia);
  }

  protected Action1<Void> getOnSignedAction() {
    return aVoid -> {
      if (presenter.getRnRForm().isSubmitted()) {
        presenter.submitRequisition();
        showMessageNotifyDialog();
      } else {
        presenter.authoriseRequisition();
      }
    };
  }

  @Override
  protected String getNotifyDialogMsg() {
    return getString(R.string.msg_requisition_signature_message_notify_mmia);
  }

  @VisibleForTesting
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveMMIARequisitionEvent(DebugMMIARequisitionEvent event) {
    final long mmiaProductNum = event.getMmiaProductNum();
    final long mmiaRegimeNum = event.getMmiaRegimeNum();
    final long mmiaThreeLineNum = event.getMmiaThreeLineNum();
    final long mmiaPatientInfoNum = event.getMmiaPatientInfoNum();
    final long mmiaTotal = event.getMmiaTotal();
    RnRForm form = presenter.getRnRForm();

    rnrFormList.removeOriginalTable();
    for (RnrFormItem formItem : form.getRnrFormItemListWrapper()) {
      formItem.setIssued(mmiaProductNum);
      formItem.setAdjustment(mmiaProductNum);
      formItem.setInventory(mmiaProductNum);
    }

    regimeWrap.removeOriginalTable();
    for (RegimenItem regimenItem : form.getRegimenItemListWrapper()) {
      regimenItem.setAmount(mmiaRegimeNum);
      regimenItem.setPharmacy(mmiaRegimeNum);
    }

    mmiaRegimeThreeLineListView.removeOriginalTable();
    for (RegimenItemThreeLines regimenItemThreeLines : form.getRegimenThreeLineListWrapper()) {
      regimenItemThreeLines.setPatientsAmount(mmiaThreeLineNum);
      regimenItemThreeLines.setPharmacyAmount(mmiaThreeLineNum);
    }

    mmiaPatientInfoListView.removeOriginalTable();
    mmiaDispensedInfoList.removeOriginalTable();
    for (BaseInfoItem item : form.getBaseInfoItemListWrapper()) {
      item.setValue(String.valueOf(mmiaPatientInfoNum));
    }

    refreshRequisitionForm(form);

    etTotalPatient.setText(String.valueOf(mmiaTotal));
    etTotalMonth.setText(String.valueOf(mmiaTotal));
  }
}