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

package org.openlmis.core.view.adapter;

import android.view.View;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.ExpiredStockCardListViewHolder;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class ExpiredStockCardListAdapter extends StockCardListAdapter {

  public ExpiredStockCardListAdapter(List<InventoryViewModel> inventoryViewModels) {
    super(inventoryViewModels, null);
  }

  @Override
  protected int getItemStockCardLayoutId() {
    return R.layout.item_expired_stock_card;
  }

  @Override
  protected StockCardViewHolder createViewHolder(View view) {
    return new ExpiredStockCardListViewHolder(view);
  }
}
