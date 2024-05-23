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
import java.util.List;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.ExpiredStockCardListLotAdapter;
import org.openlmis.core.view.adapter.StockcardListLotAdapter;
import org.openlmis.core.view.adapter.StockcardListLotAdapter.LotInfoHolder.OnItemSelectListener;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class ExpiredStockCardListViewHolder extends StockCardViewHolder {

  OnItemSelectListener onItemSelectListener;

  public ExpiredStockCardListViewHolder(View itemView, OnItemSelectListener onItemSelectListener) {
    super(itemView, null);
    this.onItemSelectListener = onItemSelectListener;
  }

  @Override
  protected void inflateData(InventoryViewModel inventoryViewModel, String queryKeyWord) {
    tvStockOnHand.setText(String.valueOf(inventoryViewModel.getStockOnHand()));
    tvProductName.setText(
        TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord,
            inventoryViewModel.getProductStyledName())
    );
    super.initStockOnHandWarning(inventoryViewModel);
  }

  @Override
  protected StockcardListLotAdapter createStockCardListAdapter(List<LotOnHand> lotOnHandList) {
    return new ExpiredStockCardListLotAdapter(lotOnHandList, onItemSelectListener);
  }
}