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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import java.util.Date;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.MMTBRequisitionPresenter;
import org.openlmis.core.presenter.MMTBRequisitionPresenter.MMTBRequisitionView;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.ViewUtil;
import org.openlmis.core.utils.keyboard.KeyboardUtil;
import org.openlmis.core.view.widget.MMTBPatientInfoList;
import org.openlmis.core.view.widget.MMTBPatientThreeLineList;
import org.openlmis.core.view.widget.MMTBRnrFormProductList;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.functions.Action1;

public class MMTBRequisitionFragment extends BaseReportFragment implements MMTBRequisitionView {

  @InjectView(R.id.rnr_form_list)
  protected MMTBRnrFormProductList rnrFormList;

  @InjectView(R.id.three_line_form)
  protected MMTBPatientThreeLineList threeLineForm;

  @InjectView(R.id.mmtb_patient_info)
  protected MMTBPatientInfoList patientInfoList;

  @InjectView(R.id.scrollview)
  protected ScrollView scrollView;

  @InjectView(R.id.mmtb_rnr_items_header_freeze)
  protected ViewGroup rnrItemsHeaderFreeze;

  @InjectView(R.id.mmtb_rnr_items_header_freeze_left)
  protected ViewGroup rnrItemsHeaderFreezeLeft;

  @InjectView(R.id.mmtb_rnr_items_header_freeze_right)
  protected ViewGroup rnrItemsHeaderFreezeRight;

  protected View containerView;
  protected int actionBarHeight;

  private long formId;
  private Date periodEndDate;
  private MMTBRequisitionPresenter presenter;


  @Override
  protected BaseReportPresenter injectPresenter() {
    presenter = RoboGuice.getInjector(requireActivity()).getInstance(MMTBRequisitionPresenter.class);
    return presenter;
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
    containerView = inflater.inflate(R.layout.fragment_mmtb_requsition, container, false);
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
    rnrItemsHeaderFreezeRight.setOnTouchListener((v, event) -> true);
    containerView.post(() -> {
      int[] initialTopLocationOfRnrForm = new int[2];
      containerView.getLocationOnScreen(initialTopLocationOfRnrForm);
      actionBarHeight = initialTopLocationOfRnrForm[1];
    });
  }

  private boolean isHistoryForm() {
    return formId != 0;
  }

  @Override
  public void onDestroyView() {
    rnrFormList.removeListenerOnDestroyView();
    super.onDestroyView();
  }

  @Override
  public void refreshRequisitionForm(RnRForm form) {
    requireActivity().setTitle(getString(R.string.label_mmtb_title,
        DateUtil.formatDateWithoutYear(form.getPeriodBegin()),
        DateUtil.formatDateWithoutYear(form.getPeriodEnd()))
    );
    scrollView.setVisibility(View.VISIBLE);
    // 1. refresh rnr form items
    initProductList(form);
    // 2. refresh three line items
    threeLineForm.setData(form.getRegimenThreeLineListWrapper());
    // 3. refresh base info
    patientInfoList.setData(form.getBaseInfoItemListWrapper());
    // 4. consider how to save treatment phase form and consumption form info.

    bindListener();
  }

  private void initProductList(RnRForm form) {
    rnrFormList.setData(form.getRnrFormItemListWrapper());
    View leftHeaderView = rnrFormList.getLeftHeaderView();
    rnrItemsHeaderFreezeLeft.addView(leftHeaderView);
    ViewGroup rightHeaderView = rnrFormList.getRightHeaderView();
    rnrItemsHeaderFreezeRight.addView(rightHeaderView);
    rnrFormList.post(() -> ViewUtil.syncViewHeight(leftHeaderView, rightHeaderView));
  }

  private void bindListener() {
    actionPanelView.setListener(getOnCompleteListener(), getOnSaveListener());
    scrollView.setOnTouchListener((v, event) -> {
      scrollView.requestFocus();
      KeyboardUtil.hideKeyboard(requireActivity());
      return false;
    });
    ViewTreeObserver verticalViewTreeObserver = scrollView.getViewTreeObserver();
    verticalViewTreeObserver.addOnScrollChangedListener(
        () -> rnrItemsHeaderFreeze.setVisibility(isNeedHideFreezeHeader() ? View.INVISIBLE : View.VISIBLE));
    rnrFormList.getRnrItemsHorizontalScrollView().setOnScrollChangedListener(
        (l, t, oldl, oldt) -> rnrItemsHeaderFreezeRight.scrollBy(l - oldl, 0));
  }

  @NonNull
  private SingleClickButtonListener getOnSaveListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        // TODO on save click
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getOnCompleteListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        // TODO on complete click
      }
    };
  }

  private boolean isNeedHideFreezeHeader() {
    int[] rnrItemsViewLocation = new int[2];
    rnrFormList.getLocationOnScreen(rnrItemsViewLocation);
    int rnrFormY = rnrItemsViewLocation[1];
    int lastItemHeight = rnrFormList.getRightViewGroup()
        .getChildAt(rnrFormList.getRightViewGroup().getChildCount() - 1).getHeight();
    int offsetY = -rnrFormY + rnrItemsHeaderFreeze.getHeight() + actionBarHeight;
    int hiddenThresholdY = rnrFormList.getHeight() - lastItemHeight;
    return offsetY > hiddenThresholdY;
  }

  @Override
  public void setProcessButtonName(String buttonName) {
    actionPanelView.setPositiveButtonText(buttonName);
  }

  @Override
  public void completeSuccess() {
    ToastUtil.show(R.string.msg_mmtb_submit_tip);
    finish();
  }

  @NonNull
  public String getSignatureDialogTitle() {
    return presenter.isDraftOrDraftMissed()
        ? getResources().getString(R.string.msg_mmtb_submit_signature)
        : getResources().getString(R.string.msg_approve_signature_mmtb);
  }

  @Override
  protected void finish() {
    requireActivity().setResult(Activity.RESULT_OK);
    super.finish();
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
    return getString(R.string.msg_requisition_signature_message_notify_mmtb);
  }
}