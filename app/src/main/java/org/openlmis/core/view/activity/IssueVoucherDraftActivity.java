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
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.CHOSEN_PROGRAM_CODE;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.IS_FROM_BULK_ISSUE;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import java.io.Serializable;
import java.util.List;
import lombok.AccessLevel;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.IssueVoucherDraftPresenter;
import org.openlmis.core.presenter.IssueVoucherDraftPresenter.IssueVoucherDraftView;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.IssueVoucherDraftProductAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.listener.OnRemoveListener;
import org.openlmis.core.view.widget.BulkEntriesSignatureDialog;
import org.openlmis.core.view.widget.SignatureDialog.DialogDelegate;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_issue_voucher_draft)
public class IssueVoucherDraftActivity extends BaseActivity implements IssueVoucherDraftView, OnRemoveListener {

  @InjectView(R.id.tv_total_amount)
  private TextView tvTotalAmount;

  @Setter(AccessLevel.PROTECTED)
  @InjectView(R.id.rv_issue_voucher_draft)
  private RecyclerView rvIssueVoucher;

  @InjectView(R.id.bt_next)
  private View btNext;

  @InjectView(R.id.cl_empty)
  private View emptyView;

  @InjectPresenter(IssueVoucherDraftPresenter.class)
  private IssueVoucherDraftPresenter issueVoucherDraftPresenter;

  public static final String ORDER_NUMBER = "ORDER_NUMBER";

  public static final String MOVEMENT_REASON_CODE = "MOVEMENT_REASON";

  private String programCode;

  IssueVoucherDraftProductAdapter issueVoucherDraftProductAdapter = new IssueVoucherDraftProductAdapter();

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_bulk_issue, menu);
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

  @Override
  public void onRefreshViewModels() {
    issueVoucherDraftProductAdapter.notifyDataSetChanged();
  }

  @Override
  public void onLoadViewModelsFailed(Throwable throwable) {
    finish();
  }

  @Override
  public void onBackPressed() {
      showConfirmDialog();
  }

  @Override
  public void onRemove(int position) {
    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        null,
        getString(R.string.msg_remove_confirm),
        getString(R.string.btn_positive),
        getString(R.string.btn_negative),
        null);
    dialogFragment.show(getSupportFragmentManager(), "bulk_issue_delete_confirm_dialog");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        issueVoucherDraftProductAdapter.removeAt(position);
      }

      @Override
      public void negativeClick(String tag) {
        // do nothing
      }
    });
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    programCode = (String) getIntent().getSerializableExtra(CHOSEN_PROGRAM_CODE);
    rvIssueVoucher.setLayoutManager(new LinearLayoutManager(this));
    issueVoucherDraftProductAdapter.setRemoveListener(this);
    btNext.setOnClickListener(getNextButtonClickListener);
    rvIssueVoucher.setAdapter(issueVoucherDraftProductAdapter);
    issueVoucherDraftProductAdapter.registerAdapterDataObserver(dataObserver);
    issueVoucherDraftProductAdapter.setNewInstance(issueVoucherDraftPresenter.getCurrentViewModels());
    issueVoucherDraftPresenter.initialViewModels(getIntent());
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ISSUE_VOUCHER_DRAFT_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_AMBER;
  }

  private void openAddProducts() {
    Intent intent = new Intent(getApplicationContext(), AddProductsToBulkEntriesActivity.class);
    intent.putExtra(IS_FROM_BULK_ISSUE, false);
    intent.putExtra(CHOSEN_PROGRAM_CODE, programCode);
    intent.putExtra(SELECTED_PRODUCTS, (Serializable) issueVoucherDraftPresenter.getAddedProductCodeList());
    addProductsLauncher.launch(intent);
  }

  private final ActivityResultLauncher<Intent> addProductsLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
          return;
        }
        issueVoucherDraftPresenter.addProducts((List<Product>)
            result.getData().getSerializableExtra(SELECTED_PRODUCTS));
      });

  private final RecyclerView.AdapterDataObserver dataObserver = new AdapterDataObserver() {
    @Override
    public void onChanged() {
      updateUI();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
      updateUI();
    }

    private void updateUI() {
      int viewModelSize = issueVoucherDraftPresenter.getCurrentViewModels().size();
      btNext.setVisibility(viewModelSize == 0 ? View.GONE : View.VISIBLE);
      emptyView.setVisibility(viewModelSize == 0 ? View.VISIBLE : View.GONE);
      tvTotalAmount.setText(getString(R.string.label_total, viewModelSize));
    }
  };

  private void showConfirmDialog() {
    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        null,
        getString(R.string.msg_issue_voucher_back_confirm),
        getString(R.string.btn_positive),
        getString(R.string.btn_negative),
        "issue_voucher_back_confirm_dialog");
    dialogFragment.show(getSupportFragmentManager(), "issue_voucher_back_confirm_dialog");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        finish();
      }

      @Override
      public void negativeClick(String tag) {
        dialogFragment.dismiss();
      }
    });
  }

  private final SingleClickButtonListener getNextButtonClickListener = new SingleClickButtonListener() {
    @Override
    public void onSingleClick(View v) {
      if (v.getId() == R.id.bt_next) {
        hideKeyboard(v);
        int position = issueVoucherDraftProductAdapter.validateAll();
        if (position >= 0) {
          rvIssueVoucher.smoothScrollToPosition(position);
        } else {
          openIssueVoucherReportPage();
        }
      }
    }
  };

  private void openIssueVoucherReportPage() {
    Intent intent = new Intent(this, IssueVoucherReportActivity.class);
    intent.putExtra(PARAM_ISSUE_VOUCHER, issueVoucherDraftPresenter.coverToPodFromIssueVoucher(programCode));
    issueVoucherReportLauncher.launch(intent);
  }

  private final ActivityResultLauncher<Intent> issueVoucherReportLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
          return;
        }
      });

  private void hideKeyboard(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(
        Context.INPUT_METHOD_SERVICE);
    if (inputMethodManager != null) {
      inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }
}
