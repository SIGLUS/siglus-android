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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.AddNonBasicProductsPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.AddNonBasicProductsAdapter;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_add_non_basic_products)
public class AddNonBasicProductsActivity extends SearchBarActivity {

  public static final String EMPTY_STRING = "";
  public static final int RESULT_CODE = 2000;
  public static final String SELECTED_NON_BASIC_PRODUCTS = "SELECTED_PRODUCTS";
  @InjectView(R.id.products_list)
  RecyclerView rvNonBasicProducts;

  @InjectView(R.id.fast_scroller)
  RecyclerViewFastScroller fastScroller;

  @InjectView(R.id.btn_cancel)
  Button btnCancel;

  @InjectView(R.id.btn_add_products)
  Button btnAddProducts;

  @InjectView(R.id.tv_total)
  TextView tvTotal;

  @InjectPresenter(AddNonBasicProductsPresenter.class)
  AddNonBasicProductsPresenter presenter;

  AddNonBasicProductsAdapter adapter;

  public void setUpFastScroller(List<NonBasicProductsViewModel> viewModels) {
    if (viewModels.isEmpty()) {
      fastScroller.setVisibility(View.GONE);
    } else {
      fastScroller.setVisibility(View.VISIBLE);
    }
    List<AlphabetItem> mAlphabetItems = new ArrayList<>();
    List<String> strAlphabets = new ArrayList<>();
    for (int i = 0; i < viewModels.size(); i++) {
      String name = viewModels.get(i).getProduct().getPrimaryName();
      if (name == null || name.isEmpty()) {
        continue;
      }

      String word = name.substring(0, 1);
      if (!strAlphabets.contains(word)) {
        strAlphabets.add(word);
        mAlphabetItems.add(new AlphabetItem(i, word, false));
      }
    }
    fastScroller.setRecyclerView(rvNonBasicProducts);
    fastScroller.setUpAlphabet(mAlphabetItems);
  }

  @Override
  public boolean onSearchStart(String query) {
    adapter.filter(query);
    setTotal(adapter.getItemCount());
    setUpFastScroller(adapter.getFilteredList());
    return false;
  }

  protected void setTotal(int total) {
    tvTotal.setText(getString(R.string.label_total, total));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initRecyclerView();
    loading(getString(R.string.add_products_loading_message));
    List<String> previouslyProductCodes = (List<String>) getIntent().getSerializableExtra(SELECTED_NON_BASIC_PRODUCTS);
    btnCancel.setOnClickListener(cancelListener());
    btnAddProducts.setOnClickListener(addProductsListener());
    Subscription subscription = presenter.getAllNonBasicProductsViewModels(previouslyProductCodes)
        .subscribe(getOnViewModelsLoadedSubscriber());
    subscriptions.add(subscription);
  }

  @Override
  protected ScreenName getScreenName() {
    return null;
  }

  @NonNull
  protected Subscriber<List<NonBasicProductsViewModel>> getOnViewModelsLoadedSubscriber() {
    return new Subscriber<List<NonBasicProductsViewModel>>() {
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
      public void onNext(List<NonBasicProductsViewModel> inventoryViewModels) {
        loaded();
        adapter.filter(EMPTY_STRING);
        setTotal(adapter.getItemCount());
        setUpFastScroller(inventoryViewModels);
        adapter.notifyDataSetChanged();
      }
    };
  }

  @NonNull
  private SingleClickButtonListener addProductsListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        List<Product> selectedProducts = new ArrayList<>();
        for (NonBasicProductsViewModel model : adapter.getModels()) {
          if (model.isChecked()) {
            selectedProducts.add(model.getProduct());
          }
        }
        Intent intent = new Intent();
        intent.putExtra(SELECTED_NON_BASIC_PRODUCTS, (Serializable) selectedProducts);
        setResult(RESULT_CODE, intent);
        finish();
      }
    };
  }

  @NonNull
  private SingleClickButtonListener cancelListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        finish();
      }
    };
  }

  private void initRecyclerView() {
    adapter = new AddNonBasicProductsAdapter(presenter.getModels());
    rvNonBasicProducts.setLayoutManager(new LinearLayoutManager(this));
    rvNonBasicProducts.setAdapter(adapter);
  }
}
