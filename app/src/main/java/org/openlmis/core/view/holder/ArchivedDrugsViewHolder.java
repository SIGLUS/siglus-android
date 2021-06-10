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
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import roboguice.inject.InjectView;

public class ArchivedDrugsViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_product_name)
  TextView tvProductName;

  @InjectView(R.id.tv_product_unit)
  TextView tvProductUnit;

  @InjectView(R.id.action_view_history)
  TextView tvViewHistory;

  @InjectView(R.id.action_archive_back)
  TextView tvArchiveBack;

  public ArchivedDrugsViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final InventoryViewModel inventoryViewModel, String queryKeyWord,
      final ArchiveStockCardListener listener) {

    tvProductName.setText(
        TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
    tvProductUnit.setText(
        TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));

    if (!inventoryViewModel.getProduct().isActive()) {
      tvArchiveBack.setVisibility(View.GONE);
    } else {
      tvArchiveBack.setVisibility(View.VISIBLE);
    }

    setActionListeners(inventoryViewModel, listener);
  }

  private void setActionListeners(final InventoryViewModel inventoryViewModel,
      final ArchiveStockCardListener listener) {
    tvViewHistory.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (listener != null) {
          listener.viewMovementHistory(inventoryViewModel.getStockCard());
        }
      }
    });

    tvArchiveBack.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (listener != null) {
          listener.archiveStockCardBack(inventoryViewModel.getStockCard());
        }
      }
    });
  }

  public interface ArchiveStockCardListener {

    void viewMovementHistory(StockCard stockCard);

    void archiveStockCardBack(StockCard stockCard);
  }
}
