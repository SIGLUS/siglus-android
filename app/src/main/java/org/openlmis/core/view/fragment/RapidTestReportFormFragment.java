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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RapidTestLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openlmis.core.R;
import org.openlmis.core.annotation.BindEventBus;
import org.openlmis.core.event.DebugMMITRequisitionEvent;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.RapidTestReportFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.RapidTestReportBodyLeftHeaderAdapter;
import org.openlmis.core.view.adapter.RapidTestReportRowAdapter;
import org.openlmis.core.view.holder.RapidTestReportGridViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.openlmis.core.view.widget.RapidTestRnrForm;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

@BindEventBus
public class RapidTestReportFormFragment extends BaseReportFragment
    implements RapidTestReportFormPresenter.RapidTestReportView {

  @InjectView(R.id.v_bottom_root)
  ViewGroup vBottomRoot;

  @InjectView(R.id.rapid_view_basic_item_header)
  LinearLayout rnrBasicItemHeader;

  @InjectView(R.id.rapid_view_basic_item_header_left)
  TextView rnrBasicItemHeaderLeft;

  @InjectView(R.id.rapid_test_rnr_form)
  protected RapidTestRnrForm rapidTestFormTop;

  @InjectView(R.id.vg_rapid_test_report_empty_header)
  ViewGroup emptyHeaderView;

  @InjectView(R.id.vg_rapid_test_report_body_left_header)
  ViewGroup rapidTestBodyLeftHeader;

  @InjectView(R.id.rapid_test_body_left_list)
  RecyclerView rapidTestBodyLeftListView;

  @InjectView(R.id.rv_rapid_report_row_item_list)
  RecyclerView rvReportRowItemListView;

  @InjectView(R.id.action_panel)
  View vActionPanel;

  RapidTestReportFormPresenter presenter;

  RapidTestReportRowAdapter rapidBodyRightAdapter;

  RapidTestReportBodyLeftHeaderAdapter rapidBodyLeftAdapter;

  @Override
  protected BaseReportPresenter injectPresenter() {
    presenter = RoboGuice.getInjector(getActivity()).getInstance(RapidTestReportFormPresenter.class);
    return presenter;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_rapid_test_report_form, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (SharedPreferenceMgr.getInstance().shouldSyncLastYearStockData()) {
      ToastUtil.showInCenter(R.string.msg_stock_movement_is_not_ready);
      finish();
      return;
    }

    loading();
    long formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0L);
    Date periodEndDate = ((Date) getActivity().getIntent()
        .getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    updateHeaderSize();
    setUpRowItems();
    setUpBodyLeftItems();
    actionPanelView.setListener(getOnCompleteClickListener(), getOnSaveClickListener());
    rvReportRowItemListView.setNestedScrollingEnabled(false);
    if (isSavedInstanceState && presenter.getViewModel() != null) {
      presenter.updateFormUI();
    } else {
      presenter.loadData(formId, periodEndDate);
    }
  }

  @Override
  public void setProcessButtonName(String buttonName) {
    actionPanelView.setPositiveButtonText(buttonName);
  }

  @Override
  public void refreshRequisitionForm(RnRForm rnRForm) {
    getActivity().setTitle(
        getString(R.string.label_mmit_title, DateUtil.formatDateWithoutYear(rnRForm.getPeriodBegin()),
            DateUtil.formatDateWithoutYear(rnRForm.getPeriodEnd())));
    RapidTestReportViewModel viewModel = presenter.getViewModel();
    if (!viewModel.getProductItems().isEmpty()) {
      rnrBasicItemHeader.setVisibility(View.VISIBLE);
      int width = (int) (rnrBasicItemHeader.getWidth() * 0.7);
      rnrBasicItemHeaderLeft.setMaxWidth(width);
      rapidTestFormTop.setVisibility(View.VISIBLE);
      rapidTestFormTop.initView(viewModel.getProductItems());
    } else {
      rnrBasicItemHeader.setVisibility(View.GONE);
      rapidTestFormTop.setVisibility(View.GONE);
    }
    actionPanelView.setVisibility(
        presenter.getViewModel().getStatus().isEditable() ? View.VISIBLE : View.GONE);
    updateButtonName();
    initListener();
    populateFormData(viewModel);
    loaded();
  }

  @Override
  public void completeSuccess() {
    ToastUtil.show(R.string.msg_rapid_test_submit_tip);
    finish();
  }

  @Override
  protected void finish() {
    getActivity().setResult(Activity.RESULT_OK);
    getActivity().finish();
  }

  private void updateHeaderSize() {
    final int rowHeaderWidth = (int) getResources().getDimension(R.dimen.rapid_view_Header_view);
    emptyHeaderView.getLayoutParams().width = rowHeaderWidth;
    rapidTestBodyLeftHeader.getLayoutParams().width = rowHeaderWidth;
  }

  private void setUpRowItems() {
    rapidBodyRightAdapter = new RapidTestReportRowAdapter(getQuantityChangeListener());
    rvReportRowItemListView.setLayoutManager(new RapidTestLayoutManager(getActivity()));
    rvReportRowItemListView.setAdapter(rapidBodyRightAdapter);
  }

  private void setUpBodyLeftItems() {
    rapidBodyLeftAdapter = new RapidTestReportBodyLeftHeaderAdapter();
    rapidTestBodyLeftListView.setLayoutManager(new LinearLayoutManager(getActivity()));
    rapidTestBodyLeftListView.setAdapter(rapidBodyLeftAdapter);
  }

  private RapidTestReportGridViewHolder.QuantityChangeListener getQuantityChangeListener() {
    return (columnCode, gridColumnCode) -> {
      presenter.getViewModel().updateTotal(columnCode, gridColumnCode);
      presenter.getViewModel().updateAPEWaring();
      rapidBodyRightAdapter.updateRowValue();
    };
  }

  @NonNull
  private SingleClickButtonListener getOnSaveClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        loading();
        Subscription subscription = presenter.createOrUpdateRapidTest()
            .subscribe(getOnSavedAction());
        subscriptions.add(subscription);
      }
    };
  }

  private Action1<? super RapidTestReportViewModel> getOnSavedAction() {
    return (Action1<RapidTestReportViewModel>) viewModel -> {
      loaded();
      finish();
    };
  }

  @NonNull
  private SingleClickButtonListener getOnCompleteClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        String errorMessage = showErrorMessage();
        if (!StringUtils.isEmpty(errorMessage)) {
          ToastUtil.show(errorMessage);
          return;
        }
        presenter.updateRnrForm();
        showSignDialog();
      }

      private String showErrorMessage() {
        String errorMessage = "";
        if (!rapidTestFormTop.isCompleted()) {
          errorMessage = getString(R.string.error_empty_rapid_test_product);
        } else if (presenter.getViewModel().isFormEmpty()) {
          errorMessage = getString(R.string.error_empty_rapid_test_list);
        } else if (!presenter.getViewModel().validate()) {
          errorMessage = presenter.getViewModel().getErrorMessage();
          rapidBodyRightAdapter.notifyDataSetChanged();
        } else if (presenter.getViewModel().validateOnlyAPES()) {
          errorMessage = getString(R.string.error_rapid_test_only_ape);
        }
        return errorMessage;
      }
    };
  }

  private void updateButtonName() {
    actionPanelView.setPositiveButtonText(
        presenter.getViewModel().isDraft() ? getResources()
            .getString(R.string.btn_submit) : getResources().getString(R.string.btn_complete));
  }

  @Override
  protected String getSignatureDialogTitle() {
    return presenter.getViewModel().isDraft() ? getResources()
        .getString(R.string.msg_rapid_test_submit_signature)
        : getResources().getString(R.string.msg_approve_signature_rapid_test);
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
    return getString(R.string.msg_requisition_signature_message_notify_rapid_test);
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initListener() {
    rapidTestBodyLeftListView.setOnTouchListener((v, event) -> true);
    combineRowItemListViewAndLeftListView();
  }

  private void combineRowItemListViewAndLeftListView() {
    rvReportRowItemListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      int moveState = RecyclerView.SCROLL_STATE_IDLE;

      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        rapidTestBodyLeftListView.scrollBy(dx, dy);
      }

      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        moveState = newState;
      }
    });
  }

  private void populateFormData(RapidTestReportViewModel viewModel) {
    rapidBodyRightAdapter.refresh(viewModel);
    rapidBodyLeftAdapter.refresh(viewModel.getItemViewModelList());
  }

  public void keyboardChanged(int keyboardHeight, Point currentTouchPoint) {
    final View rootView = getView();
    if (rootView == null) {
      return;
    }
    if (keyboardHeight <= 0) {
      //keyboard hide
      rootView.scrollTo(0, 0);
      return;
    }
    //keyboard show
    final RectF bottomRootRect = calcViewScreenLocation(vBottomRoot);
    if (bottomRootRect.contains(currentTouchPoint.x, currentTouchPoint.y)) {
      int needScrollHeight = vActionPanel == null ? keyboardHeight
          : (keyboardHeight - vActionPanel.getMeasuredHeight());
      rootView.scrollTo(0, needScrollHeight);
    }
  }

  private RectF calcViewScreenLocation(View view) {
    int[] location = new int[2];
    view.getLocationOnScreen(location);
    return new RectF(location[0], location[1], (float) location[0] + view.getWidth(),
        (float) location[1] + view.getHeight());
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveMMITRequisitionEvent(DebugMMITRequisitionEvent event) {
    final long mmitProductNum = event.getMmitProductNum();
    final long mmitReportNum = event.getMmitReportNum();

    RapidTestReportViewModel viewModel = presenter.getViewModel();
    // fill Balancete
    List<RnrFormItem> productItems = viewModel.getProductItems();
    for (RnrFormItem item : productItems) {
      item.setInitialAmount(mmitProductNum);
      item.setInventory(mmitProductNum);
    }
    //fill MMIT Report
    for (RapidTestFormItemViewModel itemViewModel : viewModel.getItemViewModelList()) {
      for (RapidTestFormGridViewModel gridViewModel : itemViewModel.getRapidTestFormGridViewModelList()) {
        gridViewModel.setConsumptionValue(String.valueOf(mmitReportNum));
        viewModel.updateTotal(gridViewModel.getColumnCode(), RapidTestGridColumnCode.CONSUMPTION);

        gridViewModel.setPositiveValue(String.valueOf(mmitReportNum));
        viewModel.updateTotal(gridViewModel.getColumnCode(), RapidTestGridColumnCode.POSITIVE);

        gridViewModel.setUnjustifiedValue(String.valueOf(mmitReportNum));
        viewModel.updateTotal(gridViewModel.getColumnCode(), RapidTestGridColumnCode.UNJUSTIFIED);
      }
    }

    refreshRequisitionForm(presenter.getRnRForm());
  }
}
