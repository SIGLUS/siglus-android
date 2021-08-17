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
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.io.Serializable;
import java.util.ArrayList;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.BulkInitialInventoryPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_bulk_initial_inventory)
public class BulkInitialInventoryActivity extends InventoryActivity<BulkInitialInventoryPresenter> {

  public static final int REQUEST_CODE = 1050;

  @InjectView(R.id.btn_add_products)
  TextView btnAddProducts;

  @Override
  public boolean validateInventory() {
    int position = ((BulkInitialInventoryAdapter) mAdapter).validateAllForCompletedClick();
    setTotal();
    if (position >= 0) {
      clearSearch();
      productListRecycleView.scrollToPosition(position);
      productListRecycleView.post(productListRecycleView::requestFocus);
      return false;
    }
    return true;
  }

  public View.OnClickListener goToAddNonBasicProductsLister() {
    return v -> {
      Intent intent = new Intent(getApplicationContext(), AddNonBasicProductsActivity.class);
      intent.putExtra(AddNonBasicProductsActivity.SELECTED_NON_BASIC_PRODUCTS,
          (Serializable) presenter.getAllAddedNonBasicProduct());
      startActivityForResult(intent, REQUEST_CODE);
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
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (areThereSelectedProducts(requestCode, resultCode, data)) {
      final ArrayList<Product> nonBasicProducts = (ArrayList<Product>) data
          .getSerializableExtra(AddNonBasicProductsActivity.SELECTED_NON_BASIC_PRODUCTS);
      presenter.addNonBasicProductsToInventory(nonBasicProducts).subscribe(
          bulkInitialInventoryViewModels -> {
            mAdapter.refresh();
            setUpFastScroller(mAdapter.getFilteredList());
            mAdapter.notifyDataSetChanged();
            setTotal();
          });

    }
  }

  @Override
  protected void setTotal() {
    int total = 0;
    for (InventoryViewModel model : presenter.getInventoryViewModelList()) {
      if (model.getProductId() != 0
          && (model.getViewType() == BulkInitialInventoryAdapter.ITEM_BASIC
          || model.getViewType() == BulkInitialInventoryAdapter.ITEM_NO_BASIC)) {
        total++;
      }
    }
    tvTotal.setText(getString(R.string.label_total, total));
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
      ToastUtil.showForLongTime(getValidateFailedTips());
    }
  }

  protected BulkInitialInventoryAdapter.RemoveNonBasicProduct removeNonBasicProductListener() {
    return viewModel -> {
      presenter.removeNonBasicProductElement(viewModel);
      mAdapter.refresh();
      mAdapter.notifyDataSetChanged();
      setTotal();
    };
  }

  private Subscriber<Object> getReloadSubscriber() {
    return new Subscriber<Object>() {
      @Override
      public void onCompleted() {
        ToastUtil.showForLongTime(R.string.successfully_saved);
        loaded();
        btnSave.setEnabled(true);
      }

      @Override
      public void onError(Throwable e) {
        // do nothing
      }

      @Override
      public void onNext(Object o) {
        Subscription loaded = presenter.loadPrograms().subscribe(getOnProgramsLoadedSubscriber());
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

  private boolean areThereSelectedProducts(int requestCode, int resultCode, Intent data) {
    return requestCode == REQUEST_CODE
        && resultCode == AddNonBasicProductsActivity.RESULT_CODE
        && data.getExtras() != null
        && data.getExtras().containsKey(AddNonBasicProductsActivity.SELECTED_NON_BASIC_PRODUCTS);
  }
}
