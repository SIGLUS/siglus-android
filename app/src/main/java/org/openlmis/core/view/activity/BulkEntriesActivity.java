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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.BulkEntriesPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkEntriesAdapter;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_bulk_entries)
public class BulkEntriesActivity extends BaseActivity {

  @InjectPresenter(BulkEntriesPresenter.class)
  BulkEntriesPresenter bulkEntriesPresenter;

  @InjectView(R.id.rv_bulk_entries_product)
  RecyclerView rvBulkEntriesProducts;

  @InjectView(R.id.tv_total)
  TextView tvTotal;

  BulkEntriesAdapter adapter;

  List<Product> addedProducts;


  @Override
  protected ScreenName getScreenName() {
    return ScreenName.BULK_ENTRIES_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initRecyclerView();
    Subscription subscription = bulkEntriesPresenter.getAllAddedBulkEntriesViewModels(addedProducts)
        .subscribe(getOnViewModelsLoadedSubscriber());
    subscriptions.add(subscription);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_bulk_entries, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_add_product) {
//      Intent intent = new Intent(this, AddProductsToBulkEntriesActivity.class);
//      startActivity(intent);
      openAddProductsActivityForResult();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  public void openAddProductsActivityForResult() {
    Intent intent = new Intent(getApplicationContext(),AddProductsToBulkEntriesActivity.class);
    intent.putExtra(SELECTED_PRODUCTS,
        (Serializable)bulkEntriesPresenter.getAddedProductIds());
    addProductsActivityResultLauncher.launch(intent);
  }

  private final ActivityResultLauncher<Intent> addProductsActivityResultLauncher = registerForActivityResult(
      new StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          addedProducts = (List<Product>) result.getData().getSerializableExtra(SELECTED_PRODUCTS);
          bulkEntriesPresenter.addNewProductsToBulkEntriesViewModels(addedProducts);
          adapter.refresh();
          adapter.notifyDataSetChanged();
        }
      });

  protected void setTotal(int total) {
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
        adapter.notifyDataSetChanged();
      }
    };
  }
}
