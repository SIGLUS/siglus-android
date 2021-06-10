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
import android.view.ViewGroup;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.openlmis.core.view.widget.PhysicalInventoryLotListView;
import roboguice.inject.InjectView;

public class PhysicalInventoryWithLotViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_product_name)
  TextView tvProductName;

  @InjectView(R.id.tv_product_unit)
  TextView tvProductUnit;

  @InjectView(R.id.ic_done)
  View icDone;

  @InjectView(R.id.tv_inventory_item_soh)
  TextView tvSOH;

  @InjectView(R.id.vg_inventory_item_soh)
  ViewGroup vgStockOnHand;

  @InjectView(R.id.view_lot_list)
  PhysicalInventoryLotListView lotListView;

  protected PhysicalInventoryViewModel viewModel;

  public PhysicalInventoryWithLotViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final PhysicalInventoryViewModel viewModel, final String queryKeyWord,
      final InventoryItemStatusChangeListener refreshCompleteCountListener) {
    this.viewModel = viewModel;
    lotListView.initLotListView(viewModel, new InventoryItemStatusChangeListener() {
      @Override
      public void onStatusChange(boolean done) {
        updateTitle(done, queryKeyWord);
        refreshCompleteCountListener.onStatusChange(done);
      }
    });
    updateTitle(viewModel.isDone(), queryKeyWord);
  }

  private void highlightQueryKeyWord(PhysicalInventoryViewModel inventoryViewModel,
      String queryKeyWord, boolean done) {
    if (done) {
      tvProductName.setText(
          TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getGreenName()));
      tvProductUnit.setText(
          TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getGreenUnit()));
    } else {
      tvProductName.setText(
          TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
      tvProductUnit.setText(
          TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));
    }
  }

  private void updateTitle(boolean done, String queryKeyWord) {
    icDone.setVisibility(done ? View.VISIBLE : View.GONE);
    highlightQueryKeyWord(viewModel, queryKeyWord, done);
    vgStockOnHand.setVisibility(done ? View.VISIBLE : View.GONE);
    if (done) {
      tvSOH.setText(String.valueOf(viewModel.getLotListQuantityTotalAmount()));
    }
  }

  public interface InventoryItemStatusChangeListener {

    void onStatusChange(boolean done);
  }
}
