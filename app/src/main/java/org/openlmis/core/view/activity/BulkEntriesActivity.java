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

import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.BulkEntriesPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkEntriesAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.openlmis.core.view.widget.BulkEntriesSignatureDialog;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_bulk_entries)
public class BulkEntriesActivity extends BaseActivity {

  public static final String KEY_FROM_BULK_ENTRIES_COMPLETE = "key from bulk entries complete";
  @InjectPresenter(BulkEntriesPresenter.class)
  BulkEntriesPresenter bulkEntriesPresenter;
  @InjectView(R.id.rv_bulk_entries_product)
  RecyclerView rvBulkEntriesProducts;
  @InjectView(R.id.tv_total)
  TextView tvTotal;
  @InjectView(R.id.btn_save)
  View btnSave;
  @InjectView(R.id.btn_complete)
  Button btnComplete;
  @InjectView(R.id.action_panel)
  View actionPanel;
  @InjectView(R.id.msg_no_product)
  TextView msgNoProduct;
  @InjectView(R.id.iv_no_product)
  ImageView ivNoProduct;
  @InjectView(R.id.v_total_divider)
  View totalDivider;
  BulkEntriesAdapter adapter;
  List<Product> addedProducts;
  private final ActivityResultLauncher<Intent> addProductsActivityResultLauncher = registerForActivityResult(
      new StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          addedProducts = (List<Product>) result.getData().getSerializableExtra(SELECTED_PRODUCTS);
          bulkEntriesPresenter.addNewProductsToBulkEntriesViewModels(addedProducts);
          adapter.refresh();
          adapter.notifyDataSetChanged();
        }
      });

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_bulk_entries, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_add_product) {
      openAddProductsActivityForResult();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (bulkEntriesPresenter.isDraftExisted()) {
      showConfirmDialog();
    } else {
      super.onBackPressed();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveRefreshStatus(String event) {
    if (event.equals(Constants.REFRESH_BACKGROUND_EVENT)) {
      setViewGoneWhenNoProduct(bulkEntriesPresenter.getBulkEntriesViewModels());
      setTotal(adapter.getItemCount());
    }
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.BULK_ENTRIES_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initRecyclerView();
    Subscription subscription = bulkEntriesPresenter.getBulkEntriesViewModelsFromDraft()
        .subscribe(getOnViewModelsLoadedSubscriber());
    subscriptions.add(subscription);
    btnSave.setOnClickListener(getSaveListener());
    btnComplete.setOnClickListener(getCompleteListener());
    EventBus.getDefault().register(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setViewGoneWhenNoProduct(bulkEntriesPresenter.getBulkEntriesViewModels());
    setTotal(adapter.getItemCount());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
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
        bulkEntriesPresenter.deleteDraft();
        finish();
      }

      @Override
      public void negativeClick(String tag) {
        // do nothing
      }
    });
  }

  private void setViewGoneWhenNoProduct(List<BulkEntriesViewModel> bulkEntriesViewModels) {
    actionPanel.setVisibility(getGoneWhenNoProduct(bulkEntriesViewModels));
    ivNoProduct.setVisibility(getVisibleWhenNoProduct(bulkEntriesViewModels));
    msgNoProduct.setVisibility(getVisibleWhenNoProduct(bulkEntriesViewModels));
    tvTotal.setVisibility(getGoneWhenNoProduct(bulkEntriesViewModels));
    totalDivider.setVisibility(getGoneWhenNoProduct(bulkEntriesViewModels));
  }

  private int getGoneWhenNoProduct(List<BulkEntriesViewModel> bulkEntriesViewModels) {
    return bulkEntriesViewModels.isEmpty() ? View.GONE : View.VISIBLE;
  }

  private int getVisibleWhenNoProduct(List<BulkEntriesViewModel> bulkEntriesViewModels) {
    return bulkEntriesViewModels.isEmpty() ? View.VISIBLE : View.GONE;
  }

  private void setTotal(int total) {
    tvTotal.setText(getString(R.string.label_total, total));
  }

  private void initRecyclerView() {
    adapter = new BulkEntriesAdapter(bulkEntriesPresenter.getBulkEntriesViewModels());
    rvBulkEntriesProducts.setLayoutManager(new LinearLayoutManager(this));
    rvBulkEntriesProducts.setAdapter(adapter);
  }

  @NonNull
  protected Subscriber<List<BulkEntriesViewModel>> getOnViewModelsLoadedSubscriber() {
    return new Subscriber<List<BulkEntriesViewModel>>() {
      @Override
      public void onCompleted() {
        // do nothing
      }

      @Override
      public void onError(Throwable e) {
        ToastUtil.show(e.getMessage());
        loaded();
      }

      @Override
      public void onNext(List<BulkEntriesViewModel> productsToBulkEntriesViewModels) {
        loaded();
        setTotal(adapter.getItemCount());
        setViewGoneWhenNoProduct(bulkEntriesPresenter.getBulkEntriesViewModels());
        adapter.notifyDataSetChanged();
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getSaveListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        hideKeyboard(btnSave);
        Subscription subscription = bulkEntriesPresenter.saveDraftBulkEntriesObservable()
            .subscribe(getReloadSubscriber());
        subscriptions.add(subscription);
        finish();
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getCompleteListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        hideKeyboard(btnComplete);
        if (doValidation()) {
          showSignDialog();
        }
      }
    };
  }

  private void showSignDialog() {
    BulkEntriesSignatureDialog signatureDialog = new BulkEntriesSignatureDialog();
    signatureDialog.setArguments(BulkEntriesSignatureDialog.getBundleToMe(DateUtil.formatDate(new Date())));
    signatureDialog.setDelegate(getSignatureDialogDelegate());
    signatureDialog.show(getSupportFragmentManager());
  }

  private boolean doValidation() {
    int firstInvalidPosition = adapter.validateAllForCompletedClick();
    adapter.notifyDataSetChanged();
    if (firstInvalidPosition >= 0) {
      rvBulkEntriesProducts.requestFocus();
      rvBulkEntriesProducts.scrollToPosition(firstInvalidPosition);
      LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rvBulkEntriesProducts.getLayoutManager();
      linearLayoutManager.scrollToPositionWithOffset(firstInvalidPosition, 0);
      return false;
    }
    return true;
  }

  private Subscriber<Object> getReloadSubscriber() {
    return new Subscriber<Object>() {
      @Override
      public void onCompleted() {
        finish();
        ToastUtil.showForLongTime(R.string.successfully_saved);
      }

      @Override
      public void onError(Throwable e) {
        ToastUtil.show(e.getMessage());
        loaded();
      }

      @Override
      public void onNext(Object o) {
        // do nothing
      }
    };
  }

  private Subscriber<Long> getSaveBulkEntriesProductsSubscriber() {
    List<Long> stockCardIdList = new ArrayList<>();
    return new Subscriber<Long>() {
      @Override
      public void onCompleted() {
        bulkEntriesPresenter.deleteDraft();
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_STOCK_CARD_ID_ARRAY,
            ArrayUtils.toPrimitive(stockCardIdList.toArray(new Long[0])));
        setResult(Activity.RESULT_OK, intent);
        loaded();
        ToastUtil.show(R.string.msg_complete_successfully);
        finish();
      }

      @Override
      public void onError(Throwable e) {
        ToastUtil.show(e.getMessage());
        loaded();
      }

      @Override
      public void onNext(Long id) {
        stockCardIdList.add(id);
      }
    };
  }

  private void openAddProductsActivityForResult() {
    Intent intent = new Intent(getApplicationContext(), AddProductsToBulkEntriesActivity.class);
    intent.putExtra(SELECTED_PRODUCTS, (Serializable) bulkEntriesPresenter.getAddedProductCodes());
    addProductsActivityResultLauncher.launch(intent);
  }

  private void hideKeyboard(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(
        Context.INPUT_METHOD_SERVICE);
    if (inputMethodManager != null) {
      inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  private SignatureDialog.DialogDelegate getSignatureDialogDelegate() {
    return new SignatureDialog.DialogDelegate() {

      @Override
      public void onSign(String signature) {
        loading();
        Subscription subscription = bulkEntriesPresenter.saveBulkEntriesProducts(signature)
            .subscribe(getSaveBulkEntriesProductsSubscriber());
        subscriptions.add(subscription);
      }
    };
  }

}
