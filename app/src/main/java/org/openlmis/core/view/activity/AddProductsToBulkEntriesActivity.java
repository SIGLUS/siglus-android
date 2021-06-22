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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.AddProductsToBulkEntriesPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.AddProductsToBulkEntriesAdapter;
import org.openlmis.core.view.viewmodel.ProductsToBulkEntriesViewModel;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_add_products_to_bulk_entries)
public class AddProductsToBulkEntriesActivity extends SearchBarActivity {

  public static final String EMPTY_STRING = "";
  public static final String SELECTED_PRODUCTS = "SELECTED_PRODUCTS";
  private List<String> previouslyProductCodes;

  AddProductsToBulkEntriesAdapter adapter;

  @InjectPresenter(AddProductsToBulkEntriesPresenter.class)
  AddProductsToBulkEntriesPresenter addProductsToBulkEntriesPresenter;

  @InjectView(R.id.tv_total)
  TextView tvTotal;

  @InjectView(R.id.bulk_entries_products)
  RecyclerView rvProducts;

  @InjectView(R.id.btn_add_products)
  Button btnAddProducts;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initRecyclerView();
    loading(getString(R.string.add_products_loading_message));
    previouslyProductCodes = (List<String>) getIntent()
        .getSerializableExtra(SELECTED_PRODUCTS);
    btnAddProducts.setOnClickListener(addProductsListener());
    Subscription subscription = addProductsToBulkEntriesPresenter.getAllProducts(new ArrayList<>())
        .subscribe(getOnViewModelsLoadedSubscriber());
    subscriptions.add(subscription);
  }

  @Override
  public boolean onSearchStart(String query) {
    adapter.filter(query);
    setTotal(adapter.getItemCount());
    return false;
  }

  @Override
  protected ScreenName getScreenName() {
    return null;
  }

  private void initRecyclerView() {
    adapter = new AddProductsToBulkEntriesAdapter(addProductsToBulkEntriesPresenter.getModels());
    rvProducts.setLayoutManager(new LinearLayoutManager(this));
    rvProducts.setAdapter(adapter);
  }

  @NonNull
  private View.OnClickListener addProductsListener() {
    return v -> {
      List<Product> selectedProducts = new ArrayList<>();
      for (ProductsToBulkEntriesViewModel model : adapter.getModels()) {
        if (model.isChecked()) {
          selectedProducts.add(model.getProduct());
        }
      }
      Intent intent = new Intent();
      intent.putExtra(SELECTED_PRODUCTS, (Serializable) selectedProducts);
      setResult(Activity.RESULT_OK,intent);
      finish();
    };
  }


  protected void setTotal(int total) {
    tvTotal.setText(getString(R.string.label_total, total));
  }

  @NonNull
  protected Subscriber<List<ProductsToBulkEntriesViewModel>> getOnViewModelsLoadedSubscriber() {
    return new Subscriber<List<ProductsToBulkEntriesViewModel>>() {
      @Override
      public void onCompleted() {
      }

      @Override
      public void onError(Throwable e) {
        ToastUtil.show(e.getMessage());
        loaded();
      }

      @Override
      public void onNext(List<ProductsToBulkEntriesViewModel> productsToBulkEntriesViewModels) {
        loaded();
        adapter.filter(EMPTY_STRING);
        setTotal(adapter.getItemCount());
        adapter.notifyDataSetChanged();
      }
    };
  }
}
