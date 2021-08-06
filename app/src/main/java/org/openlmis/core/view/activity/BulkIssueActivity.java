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

import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.IS_FROM_BULK_ISSUE;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import java.io.Serializable;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.BulkIssuePresenter;
import org.openlmis.core.presenter.BulkIssuePresenter.BulkIssueView;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkIssueAdapter;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_bulk_issue)
public class BulkIssueActivity extends BaseActivity implements BulkIssueView {

  @InjectView(R.id.tv_total_amount)
  private TextView tvTotalAmount;

  @InjectView(R.id.rv_bulk_issue)
  private RecyclerView rvBulkIssue;

  @InjectView(R.id.action_panel)
  private View actionPanel;

  @InjectView(R.id.cl_empty)
  private View emptyView;

  @InjectPresenter(BulkIssuePresenter.class)
  BulkIssuePresenter bulkIssuePresenter;

  BulkIssueAdapter bulkIssueAdapter = new BulkIssueAdapter();

  private final ActivityResultLauncher<Intent> addProductsLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
          return;
        }
        bulkIssuePresenter.addProducts((List<Product>) result.getData().getSerializableExtra(SELECTED_PRODUCTS));
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
      int viewModelSize = bulkIssuePresenter.getCurrentViewModels().size();
      actionPanel.setVisibility(viewModelSize == 0 ? View.GONE : View.VISIBLE);
      emptyView.setVisibility(viewModelSize == 0 ? View.VISIBLE : View.GONE);
      tvTotalAmount.setText(getString(R.string.label_total, viewModelSize));
    }
  };

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
    bulkIssueAdapter.notifyDataSetChanged();
  }

  @Override
  public void onLoadViewModelsFailed(Throwable throwable) {
    finish();
  }

  @Override
  public void onSaveDraftFinished(boolean succeeded) {
    if (succeeded) {
      ToastUtil.show(getString(R.string.successfully_saved));
      finish();
    } else {
      ToastUtil.show(getString(R.string.unsuccessfully_saved));
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rvBulkIssue.setLayoutManager(new LinearLayoutManager(this));
    rvBulkIssue.setAdapter(bulkIssueAdapter);
    bulkIssueAdapter.registerAdapterDataObserver(dataObserver);
    bulkIssueAdapter.setNewInstance(bulkIssuePresenter.getCurrentViewModels());
    bulkIssuePresenter.initialViewModels(getIntent());
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.BULK_ISSUE;
  }

  private void openAddProducts() {
    Intent intent = new Intent(getApplicationContext(), AddProductsToBulkEntriesActivity.class);
    intent.putExtra(SELECTED_PRODUCTS, (Serializable) bulkIssuePresenter.getAddedProductCodes());
    intent.putExtra(IS_FROM_BULK_ISSUE, true);
    addProductsLauncher.launch(intent);
  }
}