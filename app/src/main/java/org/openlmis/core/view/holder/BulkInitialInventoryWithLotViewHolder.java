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
import androidx.annotation.NonNull;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.widget.BulkInitialInventoryLotListView;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.InjectView;

public class BulkInitialInventoryWithLotViewHolder extends BaseViewHolder {

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
  BulkInitialInventoryLotListView lotListView;

  protected BulkInitialInventoryViewModel viewModel;
  private BulkInitialInventoryAdapter.RemoveNonBasicProduct removeNonBasicProductListener;

  public BulkInitialInventoryWithLotViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final BulkInitialInventoryViewModel viewModel, final String queryKeyWord,
      final BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener refreshCompleteCountListener,
      final BulkInitialInventoryAdapter.RemoveNonBasicProduct removeNonBasicProductListener) {
    this.viewModel = viewModel;
    this.removeNonBasicProductListener = removeNonBasicProductListener;
    if (lotListView == null) {
      return;
    }
    lotListView.initLotListView(viewModel, done -> {
      updateTitle(done, queryKeyWord);
      refreshCompleteCountListener.onStatusChange(done);
    }, removeProductListener);
    updateTitle(viewModel.isDone(), queryKeyWord);
  }


  @NonNull
  private final SingleClickButtonListener removeProductListener = new SingleClickButtonListener() {
    @Override
    public void onSingleClick(View v) {
      removeNonBasicProductListener
          .removeNoneBasicProduct(viewModel);
    }
  };

  private void highlightQueryKeyWord(BulkInitialInventoryViewModel inventoryViewModel,
      String queryKeyWord, boolean done) {
    if (done) {
      tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getGreenName()));
      tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getGreenUnit()));
    } else {
      tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
      tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));
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
