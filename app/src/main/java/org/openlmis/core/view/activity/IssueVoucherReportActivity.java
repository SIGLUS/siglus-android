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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Pod;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.presenter.IssueVoucherReportPresenter;
import org.openlmis.core.presenter.IssueVoucherReportPresenter.IssueVoucherView;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.IssueVoucherProductAdapter;
import org.openlmis.core.view.adapter.IssueVoucherReportAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import org.openlmis.core.view.widget.ActionPanelView;
import org.openlmis.core.view.widget.IssueVoucherSignatureDialog;
import org.openlmis.core.view.widget.OrderInfoView;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_issue_voucher_report)
public class IssueVoucherReportActivity extends BaseActivity implements IssueVoucherView {

  @Setter
  @InjectView(R.id.view_orderInfo)
  private OrderInfoView orderInfo;

  @InjectView(R.id.product_name_list_view)
  private RecyclerView rvProductList;

  @InjectView(R.id.form_list_view)
  private RecyclerView rvIssueVoucherList;

  @Getter
  @InjectView(R.id.action_panel)
  private ActionPanelView actionPanelView;

  @InjectPresenter(IssueVoucherReportPresenter.class)
  IssueVoucherReportPresenter presenter;

  @Inject
  InternetCheck internetCheck;

  @Inject
  SyncService syncService;

  private Long podId;
  private Pod pod;
  private String pageName;
  private boolean isBackToCurrentPage;
  private IssueVoucherProductAdapter productAdapter;
  private IssueVoucherReportAdapter issueVoucherReportAdapter;
  private RecyclerView.OnScrollListener[] listeners;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    podId = getIntent().getLongExtra(Constants.PARAM_ISSUE_VOUCHER_FORM_ID, 0);
    String toPage = getIntent().getStringExtra(Constants.PARAM_ISSUE_VOUCHER_OR_POD);
    pageName = toPage == null ? Constants.PARAM_ISSUE_VOUCHER : toPage;
    if (getIntent().getExtras() != null) {
      pod = (Pod) getIntent().getExtras().getSerializable(Constants.PARAM_ISSUE_VOUCHER);
      isBackToCurrentPage = ScreenName.ISSUE_VOUCHER_REPORT_SCREEN
          == getIntent().getExtras().getSerializable(IntentConstants.FROM_PAGE);
    }
    String reason = getIntent().getStringExtra(IntentConstants.PARAM_MOVEMENT_REASON_CODE);
    if (reason != null) {
      presenter.reasonCode = reason;
    }
    initProductList();
    initIssueVoucherList();
    listeners = scrollInSync(rvIssueVoucherList, rvProductList);

    if (savedInstanceState != null && presenter.getIssueVoucherReportViewModel() != null) {
      refreshIssueVoucherForm(presenter.getPod());
    } else if (pod != null) {
      presenter.loadViewModelByPod(pod, isBackToCurrentPage);
    } else {
      presenter.loadData(podId);
    }
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ISSUE_VOUCHER_REPORT_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_AMBER;
  }

  @Override
  protected void onDestroy() {
    if (listeners != null) {
      rvProductList.removeOnScrollListener(listeners[0]);
      rvIssueVoucherList.removeOnScrollListener(listeners[1]);
    }
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (pageName.equals(Constants.PARAM_ISSUE_VOUCHER) && presenter.getPod().getOrderStatus() == OrderStatus.SHIPPED) {
      showConfirmDialog();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void refreshIssueVoucherForm(Pod pod) {
    IssueVoucherReportViewModel viewModel = presenter.getIssueVoucherReportViewModel();
    orderInfo.refresh(pod, viewModel);
    productAdapter.setList(viewModel.getProductViewModels());
    issueVoucherReportAdapter.setList(viewModel.getProductViewModels());
    if (viewModel.getPodStatus() == OrderStatus.RECEIVED) {
      actionPanelView.setVisibility(View.GONE);
    } else {
      actionPanelView.setVisibility(View.VISIBLE);
      actionPanelView.setListener(getOnCompleteListener(), getOnSaveListener());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_issue_voucher_draft, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_add_product) {
      openAddProducts();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  private void openAddProducts() {
    Intent intent = new Intent(getApplicationContext(), AddProductsToBulkEntriesActivity.class);
    intent.putExtra(IntentConstants.IS_FROM_BULK_ISSUE, false);
    intent.putExtra(IntentConstants.FROM_PAGE, getScreenName());
    intent.putExtra(IntentConstants.PARAM_CHOSEN_PROGRAM_CODE,
        presenter.getIssueVoucherReportViewModel().getProgram().getProgramCode());
    intent.putExtra(IntentConstants.PARAM_SELECTED_PRODUCTS,
        (Serializable) presenter.getAddedProductCodeList());
    startActivity(intent);
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

  private void showConfirmDialog() {
    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        null,
        getString(R.string.msg_back_confirm),
        getString(R.string.btn_positive),
        getString(R.string.btn_negative),
        "back_confirm_dialog");
    dialogFragment.show(getSupportFragmentManager(), "back_confirm_dialog");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        presenter.deleteIssueVoucher();
        backToIssueVoucherListActivity();
      }

      @Override
      public void negativeClick(String tag) {
      }
    });
  }

  @NonNull
  private SingleClickButtonListener getOnCompleteListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        int position = issueVoucherReportAdapter.validateAll();
        if (position >= 0) {
          rvIssueVoucherList.requestFocus();
          rvIssueVoucherList.post(() -> rvIssueVoucherList.scrollToPosition(position));
          rvProductList.post(() -> {
            rvProductList.removeOnScrollListener(listeners[1]);
            rvProductList.scrollToPosition(position);
            rvProductList.removeOnScrollListener(listeners[1]);
          });
        } else {
          showSignDialog();
        }
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getOnSaveListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        loading();
        Subscription subscription = presenter.getSaveFormObservable()
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
        backToIssueVoucherListActivity();
      }

      @Override
      public void onError(Throwable e) {
        loaded();
        ToastUtil.show(getString(R.string.hint_save_issue_voucher_failed));
      }

      @Override
      public void onNext(Void aVoid) {
        // do nothing
      }
    };
  }

  protected void showSignDialog() {
    IssueVoucherSignatureDialog signatureDialog = new IssueVoucherSignatureDialog();
    signatureDialog.setArguments(IssueVoucherSignatureDialog.getBundleToMe(DateUtil.formatDate(new Date()),
        presenter.getIssueVoucherReportViewModel().getProgram().getProgramName()));
    signatureDialog.setDelegate(getSignatureDialogDelegate());
    signatureDialog.show(getSupportFragmentManager());
  }

  private IssueVoucherSignatureDialog.DialogDelegate getSignatureDialogDelegate() {
    return new IssueVoucherSignatureDialog.DialogDelegate() {
      @Override
      public void onSign(String deliveredBy, String receivedBy) {
        loading();
        Subscription subscription = presenter.getCompleteFormObservable(deliveredBy, receivedBy)
            .subscribe(getCompleteIssueVoucherSubscriber());
        subscriptions.add(subscription);
      }
    };
  }

  private Subscriber<Void> getCompleteIssueVoucherSubscriber() {
    return new Subscriber<Void>() {
      @Override
      public void onCompleted() {
        loaded();
        ToastUtil.show(R.string.msg_complete_successfully);
        internetCheck.execute(checkInternetListener());
        backToPodListActivity();
      }

      @Override
      public void onError(Throwable e) {
        ToastUtil.show(e.getMessage());
        loaded();
      }

      @Override
      public void onNext(Void aVoid) {
      }
    };
  }

  private InternetCheck.Callback checkInternetListener() {

    return internet -> {
      if (Boolean.TRUE.equals(internet)) {
        syncService.requestSyncImmediatelyByTask();
      } else {
        Log.d("Internet", "No hay conexion");
      }
    };
  }

  private void backToIssueVoucherListActivity() {
    Intent intent = new Intent(this, IssueVoucherListActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

  private void backToPodListActivity() {
    Intent intent = new Intent(this, IssueVoucherListActivity.class);
    intent.putExtra(IntentConstants.PARAM_ISSUE_VOUCHER_LIST_PAGE, 1);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

}
