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

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.io.Serializable;
import java.util.ArrayList;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openlmis.core.R;
import org.openlmis.core.annotation.BindEventBus;
import org.openlmis.core.event.DebugInitialInventoryEvent;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.BulkInitialInventoryPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@BindEventBus
@ContentView(R.layout.activity_bulk_initial_inventory)
public class BulkInitialInventoryActivity extends InventoryActivity<BulkInitialInventoryPresenter> {

  public static final int REQUEST_CODE = 1050;

  public static final String KEY_FROM_INITIAL_INVENTORY = "Initial Inventory";

  @InjectView(R.id.btn_add_products)
  TextView btnAddProducts;

  public ActivityResultCallback<ActivityResult> addProductsResultCallback =
      new ActivityResultCallback<ActivityResult>() {
    @Override
    public void onActivityResult(ActivityResult result) {
      int resultCode = result.getResultCode();
      Intent data = result.getData();
      if (areThereSelectedProducts(resultCode, data)) {
        final ArrayList<Product> nonBasicProducts = (ArrayList<Product>) data
            .getSerializableExtra(AddNonBasicProductsActivity.SELECTED_NON_BASIC_PRODUCTS);
        presenter.addNonBasicProductsObservable(nonBasicProducts).subscribe(
            bulkInitialInventoryViewModels -> {
              mAdapter.refresh();
              setUpFastScroller(mAdapter.getFilteredList());
            });
      }
    }
  };

  private final ActivityResultLauncher<Intent> addProductsLauncher = registerForActivityResult(
      new StartActivityForResult(), addProductsResultCallback);

  @Override
  public boolean validateInventory() {
    int position = ((BulkInitialInventoryAdapter) mAdapter).validateAllForCompletedClick();
    if (position >= 0) {
      clearSearch();
      productListRecycleView.scrollToPosition(position);
      productListRecycleView.post(productListRecycleView::requestFocus);
      return false;
    }
    return true;
  }

  public SingleClickButtonListener goToAddNonBasicProductsLister() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        Intent intent = new Intent(getApplicationContext(), AddNonBasicProductsActivity.class);
        intent.putExtra(AddNonBasicProductsActivity.SELECTED_NON_BASIC_PRODUCTS,
            (Serializable) presenter.getAllAddedNonBasicProduct());
        addProductsLauncher.launch(intent);
      }
    };
  }

  @Override
  public void onBackPressed() {
    if (isSearchViewActivity()) {
      searchView.onActionViewCollapsed();
      return;
    }
    if (isDataChange()) {
      showDataChangeConfirmDialog();
      return;
    }
    super.onBackPressed();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveInitialInventory(DebugInitialInventoryEvent event) {
    loading();
    presenter.autofillAllProductInventory(event).subscribe(o -> {
      loaded();
      mAdapter.refresh();
    });
  }

  @Override
  protected boolean enableFilter() {
    return false;
  }

  @Override
  protected BulkInitialInventoryPresenter initPresenter() {
    return RoboGuice.getInjector(this).getInstance(BulkInitialInventoryPresenter.class);
  }

  @Override
  protected void initUI() {
    super.initUI();
    btnAddProducts.setOnClickListener(goToAddNonBasicProductsLister());
  }

  @Override
  protected void initRecyclerView() {
    mAdapter = new BulkInitialInventoryAdapter(presenter.getInventoryViewModelList(),
        removeNonBasicProductListener(),
        done -> setTotal());
    productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
    productListRecycleView.setAdapter(mAdapter);
  }

  @Override
  protected void goToNextPage() {
    preferencesMgr.setIsNeedsInventory(false);
    startActivity(HomeActivity.getIntentToMe(this));
    this.finish();
  }

  @Override
  protected void setTotal() {
    String formattedTotal;
    if (isInSearching()) {
      formattedTotal = getString(R.string.label_total, getTotalCount());
    } else {
      formattedTotal = getString(R.string.label_total_complete_counts, getCompleteCount(), getTotalCount());
    }
    tvTotal.setText(formattedTotal);
  }

  @Override
  protected void onSaveClick() {
    super.onSaveClick();
    btnSave.setEnabled(false);
    loading();
    Subscription subscription = presenter.saveDraftInventoryObservable().subscribe(getReloadSubscriber());
    subscriptions.add(subscription);
  }

  @Override
  protected void onCompleteClick() {
    super.onCompleteClick();
    btnDone.setEnabled(false);
    if (validateInventory()) {
      loading();
      Subscription subscription = presenter.doInventory().subscribe(onNextMainPageAction);
      subscriptions.add(subscription);
    } else {
      btnDone.setEnabled(true);
      ToastUtil.show(getValidateFailedTips());
    }
  }

  protected BulkInitialInventoryAdapter.RemoveNonBasicProduct removeNonBasicProductListener() {
    return viewModel -> {
      presenter.removeNonBasicProductElement(viewModel);
      mAdapter.refresh();
    };
  }

  private Subscriber<Object> getReloadSubscriber() {
    return new Subscriber<Object>() {
      @Override
      public void onCompleted() {
        ToastUtil.showSystem(getString(R.string.successfully_saved));
        loaded();
        btnSave.setEnabled(true);
      }

      @Override
      public void onError(Throwable e) {
        // do nothing
      }

      @Override
      public void onNext(Object o) {
        Subscription loaded = presenter.getInflatedInventoryOnMainThread()
            .subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(loaded);
      }
    };
  }

  private boolean isDataChange() {
    return ((BulkInitialInventoryAdapter) mAdapter).isHasDataChanged();
  }

  private void showDataChangeConfirmDialog() {
    DialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        null,
        getString(R.string.msg_back_confirm),
        getString(R.string.btn_positive),
        getString(R.string.btn_negative),
        "onBackPressed");
    dialogFragment.show(getSupportFragmentManager(), "back_confirm_dialog");
  }

  //mark
  private boolean areThereSelectedProducts(int resultCode, Intent data) {
    return resultCode == AddNonBasicProductsActivity.RESULT_CODE
        && data.getExtras() != null
        && data.getExtras().containsKey(AddNonBasicProductsActivity.SELECTED_NON_BASIC_PRODUCTS);
  }

  private int getCompleteCount() {
    return FluentIterable.from(mAdapter.getData())
        .filter(inventoryViewModel -> inventoryViewModel != null
            && inventoryViewModel.getProductId() != 0
            && inventoryViewModel.getViewType() != BulkInitialInventoryAdapter.ITEM_BASIC_HEADER
            && inventoryViewModel.getViewType() != BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER
            && ((BulkInitialInventoryViewModel) inventoryViewModel).isDone())
        .toList().size();
  }

  private int getTotalCount() {
    if (isInSearching()) {
      return FluentIterable.from(mAdapter.getFilteredList())
          .filter(inventoryViewModel -> inventoryViewModel != null
              && inventoryViewModel.getProductId() != 0
              && inventoryViewModel.getViewType() != BulkInitialInventoryAdapter.ITEM_BASIC_HEADER
              && inventoryViewModel.getViewType() != BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER)
          .toList().size();
    } else {
      return FluentIterable.from(mAdapter.getData())
          .filter(inventoryViewModel -> inventoryViewModel != null
              && inventoryViewModel.getProductId() != 0
              && inventoryViewModel.getViewType() != BulkInitialInventoryAdapter.ITEM_BASIC_HEADER
              && inventoryViewModel.getViewType() != BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER)
          .toList().size();
    }
  }
}
