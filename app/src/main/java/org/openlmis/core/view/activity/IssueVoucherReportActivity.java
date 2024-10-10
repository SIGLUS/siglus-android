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

import static org.openlmis.core.utils.Constants.PARAM_ISSUE_VOUCHER;
import static org.openlmis.core.view.widget.DoubleRecycleViewScrollListener.scrollInSync;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.Inject;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Product;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.InternetCheckListener;
import org.openlmis.core.presenter.IssueVoucherReportPresenter;
import org.openlmis.core.presenter.IssueVoucherReportPresenter.IssueVoucherView;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.keyboard.KeyboardUtil;
import org.openlmis.core.view.adapter.IssueVoucherProductAdapter;
import org.openlmis.core.view.adapter.IssueVoucherReportAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.listener.OnUpdatePodListener;
import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.ActionPanelView;
import org.openlmis.core.view.widget.AddLotDialogFragment;
import org.openlmis.core.view.widget.IssueVoucherSignatureDialog;
import org.openlmis.core.view.widget.OrderInfoView;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_issue_voucher_report)
public class IssueVoucherReportActivity extends BaseActivity implements IssueVoucherView, OnUpdatePodListener {

  @InjectPresenter(IssueVoucherReportPresenter.class)
  IssueVoucherReportPresenter presenter;
  @Inject
  InternetCheck internetCheck;
  @Inject
  SyncService syncService;
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
  private Menu addProductMenu;
  private boolean isVisible = false;
  private Pod pod;
  private final ActivityResultLauncher<Intent> addProductPageLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
          pod = (Pod) result.getData().getSerializableExtra(PARAM_ISSUE_VOUCHER);
          presenter.loadViewModelByPod(pod, true);
        }
      });
  private String pageName;
  private boolean isBackToCurrentPage;
  private IssueVoucherProductAdapter productAdapter;
  private IssueVoucherReportAdapter issueVoucherReportAdapter;
  private RecyclerView.OnScrollListener[] listeners;

  private AddLotDialogFragment addLotDialogFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String toPage = getIntent().getStringExtra(Constants.PARAM_ISSUE_VOUCHER_OR_POD);
    pageName = toPage == null ? Constants.PARAM_ISSUE_VOUCHER : toPage;
    if (getIntent().getExtras() != null) {
      pod = (Pod) getIntent().getExtras().getSerializable(Constants.PARAM_ISSUE_VOUCHER);
      isBackToCurrentPage = ScreenName.ISSUE_VOUCHER_REPORT_SCREEN
          == getIntent().getExtras().getSerializable(IntentConstants.FROM_PAGE);
    }
    initProductList();
    initIssueVoucherList();
    issueVoucherReportAdapter.setOnUpdatePodListener(this);
    productAdapter.setProductRemoveListener(this);
    listeners = scrollInSync(rvIssueVoucherList, rvProductList);

    if (savedInstanceState != null && presenter.getIssueVoucherReportViewModel() != null) {
      refreshIssueVoucherForm(presenter.getPod());
    } else if (pod != null) {
      presenter.loadViewModelByPod(pod, isBackToCurrentPage);
    } else {
      presenter.loadData(getIntent().getLongExtra(Constants.PARAM_ISSUE_VOUCHER_FORM_ID, 0));
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
    productAdapter.setList(viewModel.getViewModels());
    issueVoucherReportAdapter.setList(viewModel.getViewModels());
    if (viewModel.getPodStatus() == OrderStatus.RECEIVED) {
      actionPanelView.setVisibility(View.GONE);
      setTitle(LMISApp.getContext().getString(R.string.title_pod, viewModel.getProgram().getProgramName()));
      orderInfo.refresh(pod, viewModel);
    } else {
      setTitle(LMISApp.getContext().getString(R.string.title_issue_voucher, viewModel.getProgram().getProgramName()));
      if (viewModel.getIsLocal()) {
        orderInfo.setVisibility(View.GONE);
      } else {
        orderInfo.refresh(pod, viewModel);
      }
      actionPanelView.setVisibility(View.VISIBLE);
      actionPanelView.setListener(getOnCompleteListener(), getOnSaveListener());
    }
    updateMenuStatus();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_add_products, menu);
    addProductMenu = menu;
    MenuItem item = addProductMenu.findItem(R.id.action_add_product);
    item.setVisible(isVisible);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    if (R.id.action_add_product == item.getItemId()) {
      openAddProducts();
      return true;
    }
    return false;
  }

  @NonNull
  public Subscriber<Void> getOnSavedSubscriber() {
    return new Subscriber<Void>() {
      @Override
      public void onCompleted() {
        loaded();
        ToastUtil.show(R.string.successfully_saved);
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

  @Override
  public void onRemove(int position) {
    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        null,
        getString(R.string.msg_remove_confirm),
        getString(R.string.btn_positive),
        getString(R.string.btn_negative),
        null);
    dialogFragment.show(getSupportFragmentManager(), "issue_voucher_delete_confirm_dialog");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        removeProduct(position);
      }

      @Override
      public void negativeClick(String tag) {
        dialogFragment.dismiss();
      }
    });
  }

  @Override
  public void onRemove(int productPosition, int lotPosition) {
    if (presenter.getIssueVoucherReportViewModel().isNeedRemoveProduct(productPosition)) {
      removeProduct(productPosition);
      return;
    }
    removeLot(productPosition, lotPosition);
  }

  @Override
  public void onUpdateTotalValue() {
    presenter.getIssueVoucherReportViewModel().updateTotalViewModels();
    issueVoucherReportAdapter.notifyItemChanged(presenter.getIssueVoucherReportViewModel().getListSize() - 1);
  }

  @Override
  public void onAddLot(IssueVoucherReportProductViewModel productViewModel) {
    showAddLotDialogFragment(productViewModel);
  }

  private boolean showAddLotDialogFragment(IssueVoucherReportProductViewModel productViewModel) {
    Bundle bundle = new Bundle();
    Product product = productViewModel.getProduct();
    bundle.putString(Constants.PARAM_STOCK_NAME, product.getFormattedProductName());

    addLotDialogFragment = new AddLotDialogFragment();
    addLotDialogFragment.setArguments(bundle);
    addLotDialogFragment.setListener(getAddNewLotDialogOnClickListener(productViewModel));
    addLotDialogFragment.setAddLotWithoutNumberListener(
        getAddLotWithoutNumberListener(productViewModel, product.getCode()));
    addLotDialogFragment.show(getSupportFragmentManager(),
        "IssueVoucherReportActivity_Add-New-Lot");
    return true;
  }

  private AddLotDialogFragment.AddLotWithoutNumberListener getAddLotWithoutNumberListener(
      IssueVoucherReportProductViewModel productViewModel, String productCode
  ) {
    return expiryDate -> {
      String lotNumber = LotMovementViewModel
          .generateLotNumberForProductWithoutLot(productCode, expiryDate);
      List<String> existedLotNumbers = productViewModel.getLotNumbers();
      if (existedLotNumbers != null && existedLotNumbers.contains(lotNumber)) {
        ToastUtil.show(
            LMISApp.getContext().getString(R.string.error_lot_without_number_already_exists));
      } else {
        presenter.addNewLot(productViewModel, lotNumber, expiryDate);

        productAdapter.notifyDataSetChanged();
        issueVoucherReportAdapter.notifyDataSetChanged();
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getAddNewLotDialogOnClickListener(
      IssueVoucherReportProductViewModel productViewModel) {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View view) {
        switch (view.getId()) {
          case R.id.btn_complete:
            KeyboardUtil.hideKeyboard(view);
            if (addLotDialogFragment.validate()
                && !addLotDialogFragment.hasIdenticalLot(productViewModel.getLotNumbers())) {

              presenter.addNewLot(
                  productViewModel, addLotDialogFragment.getLotNumber(), addLotDialogFragment.getExpiryDate()
              );

              productAdapter.notifyDataSetChanged();
              issueVoucherReportAdapter.notifyDataSetChanged();

              addLotDialogFragment.dismiss();
            }
            break;
          case R.id.btn_cancel:
            addLotDialogFragment.dismiss();
            break;
          default:
            // do nothing
        }
      }
    };
  }

  protected void showSignDialog() {
    IssueVoucherSignatureDialog signatureDialog = new IssueVoucherSignatureDialog();
    signatureDialog.setArguments(IssueVoucherSignatureDialog.getBundleToMe(DateUtil.formatDate(
        DateUtil.getCurrentDate()), presenter.getIssueVoucherReportViewModel().getProgram().getProgramName()));
    signatureDialog.setDelegate(getSignatureDialogDelegate());
    signatureDialog.show(getSupportFragmentManager());
  }

  private IssueVoucherSignatureDialog.DialogDelegate getSignatureDialogDelegate() {
    return receivedBy -> {
      loading();
      Subscription subscription = presenter.getCompleteFormObservable(receivedBy)
          .subscribe(getCompleteIssueVoucherSubscriber());
      subscriptions.add(subscription);
    };
  }

  private Subscriber<Void> getCompleteIssueVoucherSubscriber() {
    return new Subscriber<Void>() {
      @Override
      public void onCompleted() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
          syncService.requestSyncImmediatelyByTask();
          loaded();
          ToastUtil.show(R.string.msg_complete_successfully);
          backToPodListActivity();
        } else {
          internetCheck.check(checkInternetListener());
        }
      }

      @Override
      public void onError(Throwable e) {
        ToastUtil.show(e.getMessage());
        loaded();
      }

      @Override
      public void onNext(Void aVoid) {
        // do nothing
      }
    };
  }

  private InternetCheckListener checkInternetListener() {
    return internet -> {
      if (internet) {
        syncService.requestSyncImmediatelyByTask();
      } else {
        Log.w("Internet", "No hay conexion");
      }
      loaded();
      ToastUtil.show(R.string.msg_complete_successfully);
      backToPodListActivity();
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

  private void removeProduct(int position) {
    presenter.getIssueVoucherReportViewModel().removeProductAtPosition(position);
    productAdapter.removeAt(position);
    issueVoucherReportAdapter.removeAt(position);
    issueVoucherReportAdapter.notifyItemChanged(presenter.getIssueVoucherReportViewModel().getListSize() - 1);

  }

  private void removeLot(int productPosition, int lotPosition) {
    presenter.getIssueVoucherReportViewModel().removeLotAtPosition(productPosition, lotPosition);
    productAdapter.notifyDataSetChanged();
    issueVoucherReportAdapter.notifyDataSetChanged();
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

  private void updateMenuStatus() {
    isVisible = presenter.getIssueVoucherReportViewModel().getPod().isLocal()
        && presenter.getIssueVoucherReportViewModel().getPod().isDraft();
    if (addProductMenu != null) {
      addProductMenu.findItem(R.id.action_add_product).setVisible(isVisible);
    }
  }

  private void openAddProducts() {
    presenter.updatePodItems();
    Intent intent = new Intent(getApplicationContext(), AddProductsToBulkEntriesActivity.class);
    intent.putExtra(IntentConstants.IS_FROM_BULK_ISSUE, false);
    intent.putExtra(IntentConstants.FROM_PAGE, getScreenName());
    intent.putExtra(IntentConstants.PARAM_CHOSEN_PROGRAM_CODE,
        presenter.getIssueVoucherReportViewModel().getProgram().getProgramCode());
    intent.putExtra(IntentConstants.PARAM_SELECTED_PRODUCTS,
        (Serializable) presenter.getAddedProductCodeList());
    addProductPageLauncher.launch(intent);
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
        // do nothing
      }
    });
  }
}
