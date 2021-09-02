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

import static org.openlmis.core.view.widget.DoubleRecycleViewScrollListener.scrollInSync;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Pod;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.presenter.IssueVoucherReportPresenter;
import org.openlmis.core.presenter.IssueVoucherReportPresenter.IssueVoucherView;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.IssueVoucherProductAdapter;
import org.openlmis.core.view.adapter.IssueVoucherReportAdapter;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import org.openlmis.core.view.widget.ActionPanelView;
import org.openlmis.core.view.widget.OrderInfoView;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_issue_voucher_report)
public class IssueVoucherReportActivity extends BaseActivity implements IssueVoucherView {
  private static final String TAG = LmisSqliteOpenHelper.class.getSimpleName();

  @InjectView(R.id.view_orderInfo)
  private OrderInfoView orderInfo;

  @InjectView(R.id.product_name_list_view)
  private RecyclerView rvProductList;

  @InjectView(R.id.form_list_view)
  private RecyclerView rvIssueVoucherList;

  @InjectView(R.id.action_panel)
  private ActionPanelView actionPanelView;

  @InjectPresenter(IssueVoucherReportPresenter.class)
  IssueVoucherReportPresenter presenter;

  private Long podId;
  private Pod pod;
  private IssueVoucherProductAdapter productAdapter;
  private IssueVoucherReportAdapter issueVoucherReportAdapter;
  private RecyclerView.OnScrollListener[] listeners;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    podId = getIntent().getLongExtra(Constants.PARAM_ISSUE_VOUCHER_FORM_ID, 0);
    pod = (Pod) getIntent().getExtras().getSerializable(Constants.PARAM_ISSUE_VOUCHER);
    initProductList();
    initIssueVoucherList();
    listeners = scrollInSync(rvProductList, rvIssueVoucherList);

    if (savedInstanceState != null && presenter.getIssueVoucherReportViewModel() != null) {
      refreshIssueVoucherForm(presenter.pod);
    } else if (pod != null) {
      presenter.loadViewModelByPod(pod);
    } else {
      presenter.loadData(podId);
    }
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ISSUE_VOUCHER_REPORT_SCREEN;
  }

  @Override
  protected void onDestroy() {
    rvProductList.removeOnScrollListener(listeners[0]);
    rvIssueVoucherList.removeOnScrollListener(listeners[1]);
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  @Override
  public void refreshIssueVoucherForm(Pod pod) {
    orderInfo.refresh(pod);
    IssueVoucherReportViewModel viewModel = presenter.getIssueVoucherReportViewModel();
    productAdapter.setList(viewModel.getProductViewModels());
    issueVoucherReportAdapter.setList(viewModel.getProductViewModels());
    if (viewModel.getPodStatus() == OrderStatus.RECEIVED) {
      actionPanelView.setVisibility(View.GONE);
    } else {
      actionPanelView.setVisibility(View.VISIBLE);
      actionPanelView.setListener(getOnCompleteListener(), getOnSaveListener());
    }
  }

  private void initIssueVoucherList() {
    rvIssueVoucherList.setLayoutManager(new LinearLayoutManager(this));
    issueVoucherReportAdapter = new IssueVoucherReportAdapter();
    rvIssueVoucherList.setAdapter(issueVoucherReportAdapter);
  }

  private void initProductList() {
    rvProductList.setLayoutManager(new LinearLayoutManager(this));
    productAdapter = new IssueVoucherProductAdapter();
    rvProductList.setAdapter(productAdapter);
  }

  @NonNull
  private SingleClickButtonListener getOnCompleteListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        hideKeyboard(v);
        int position = issueVoucherReportAdapter.validateAll();
        if (position >= 0) {
          rvIssueVoucherList.post(() -> rvIssueVoucherList.scrollToPosition(position));
          rvProductList.post(() -> rvProductList.scrollToPosition(position));
        } else {
          Log.i(TAG, "complete");
        }
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getOnSaveListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        Log.i(TAG, "save");
      }
    };
  }

  private void hideKeyboard(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(
        Context.INPUT_METHOD_SERVICE);
    if (inputMethodManager != null) {
      inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

}
