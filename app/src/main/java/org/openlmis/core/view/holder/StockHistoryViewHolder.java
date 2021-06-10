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

package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.openlmis.core.R;
import org.openlmis.core.view.adapter.MovementItemListAdapter;
import org.openlmis.core.view.viewmodel.StockHistoryViewModel;
import roboguice.inject.InjectView;

public class StockHistoryViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_product_name)
  TextView tvProductName;

  @InjectView(R.id.tv_product_unit)
  TextView tvProductUnit;

  @InjectView(R.id.rv_stock_movement_item_list)
  RecyclerView movementHistoryListView;

  private StockHistoryViewModel viewModel;

  public StockHistoryViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(StockHistoryViewModel viewModel) {
    this.viewModel = viewModel;
    populateProductInfo();
    initRecyclerView();
  }

  private void initRecyclerView() {
    movementHistoryListView.setLayoutManager(new LinearLayoutManager(context));
    movementHistoryListView
        .setAdapter(new MovementItemListAdapter(viewModel.getFilteredMovementItemViewModelList()));
    movementHistoryListView.setNestedScrollingEnabled(false);
  }

  private void populateProductInfo() {
    tvProductName.setText(viewModel.getStyledProductName());
    tvProductUnit.setText(viewModel.getStyledProductUnit());
  }
}
