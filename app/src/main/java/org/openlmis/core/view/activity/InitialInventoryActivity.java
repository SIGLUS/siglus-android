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

import android.content.Context;
import android.content.Intent;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.openlmis.core.R;
import org.openlmis.core.presenter.InitialInventoryPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.InitialInventoryAdapter;
import org.openlmis.core.view.holder.InitialInventoryViewHolder;
import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import rx.Subscription;

@ContentView(R.layout.activity_initial_inventory)
public class InitialInventoryActivity extends InventoryActivity<InitialInventoryPresenter> {

  protected boolean isAddNewDrug;

  public static final String KEY_ADD_NEW_PRODUCT = "Add new product complete";

  protected InitialInventoryViewHolder.ViewHistoryListener viewHistoryListener = stockCard -> startActivity(
      StockMovementHistoryActivity.getIntentToMe(InitialInventoryActivity.this,
          stockCard.getId(),
          stockCard.getProduct().getFormattedProductName(),
          true,
          false));

  public static Intent getIntentToMe(Context context, boolean isAddNewDrug) {
    return new Intent(context, InitialInventoryActivity.class).putExtra(Constants.PARAM_IS_ADD_NEW_DRUG, isAddNewDrug);
  }

  @Override
  public void goToNextPage() {
    preferencesMgr.setIsNeedsInventory(false);
    startActivity(isAddNewDrug ? StockCardListActivity.getIntentToMe(this) : HomeActivity.getIntentToMe(this));
    this.finish();
  }

  @Override
  public void onBackPressed() {
    if (isSearchViewActivity()) {
      searchView.onActionViewCollapsed();
      return;
    }
    if (!isAddNewDrug) {
      ToastUtil.show(R.string.msg_save_before_exit);
      return;
    }
    super.onBackPressed();
  }

  @Override
  protected boolean enableFilter() {
    return false;
  }

  @Override
  protected InitialInventoryPresenter initPresenter() {
    return RoboGuice.getInjector(this).getInstance(InitialInventoryPresenter.class);
  }

  @Override
  protected void initUI() {
    super.initUI();
    isAddNewDrug = getIntent().getBooleanExtra(Constants.PARAM_IS_ADD_NEW_DRUG, false);
    btnSave.setVisibility(View.GONE);
    if (isAddNewDrug) {
      setTitle(getResources().getString(R.string.title_add_new_drug));
    } else if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }
  }

  @Override
  protected void initRecyclerView() {
    mAdapter = new InitialInventoryAdapter(presenter.getInventoryViewModelList(), viewHistoryListener);
    productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
    productListRecycleView.setAdapter(mAdapter);
  }

  @Override
  protected void setTotal() {
    tvTotal.setText(getString(R.string.label_total, mAdapter.getItemCount()));
  }

  @Override
  protected void onCompleteClick() {
    super.onCompleteClick();
    btnDone.setEnabled(false);
    if (validateInventory()) {
      loading();
      Subscription subscription = presenter.initStockCardObservable().subscribe(onNextMainPageAction);
      subscriptions.add(subscription);
    } else {
      btnDone.setEnabled(true);
    }
  }
}
