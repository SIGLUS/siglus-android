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


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class StockCardListAdapter extends InventoryListAdapter<StockCardViewHolder> {

  protected StockCardViewHolder.OnItemViewClickListener listener;

  public StockCardListAdapter(List<InventoryViewModel> inventoryViewModel,
      StockCardViewHolder.OnItemViewClickListener listener) {
    super(inventoryViewModel);
    this.listener = listener;
  }

  @Override
  public StockCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_stockcard, parent, false);

    return createViewHolder(view);
  }

  @Override
  public void onBindViewHolder(StockCardViewHolder holder, final int position) {
    final InventoryViewModel inventoryViewModel = filteredList.get(position);
    holder.populate(inventoryViewModel, queryKeyWord);
  }

  public void sortBySOH(final boolean asc) {

    Comparator<InventoryViewModel> stockCardComparator = (lhs, rhs) -> {
      if (asc) {
        return (int) (lhs.getStockOnHand() - rhs.getStockOnHand());
      } else {
        return (int) (rhs.getStockOnHand() - lhs.getStockOnHand());
      }
    };

    Collections.sort(filteredList, stockCardComparator);
    Collections.sort(data, stockCardComparator);

    this.notifyDataSetChanged();
  }

  public void sortByName(final boolean asc) {

    Comparator<InventoryViewModel> stockCardComparator = (lhs, rhs) -> {
      if (asc) {
        return lhs.getProduct().getPrimaryName().compareTo(rhs.getProduct().getPrimaryName());
      } else {
        return rhs.getProduct().getPrimaryName().compareTo(lhs.getProduct().getPrimaryName());
      }
    };

    Collections.sort(filteredList, stockCardComparator);
    Collections.sort(data, stockCardComparator);

    this.notifyDataSetChanged();
  }

  protected StockCardViewHolder createViewHolder(View view) {
    return new StockCardViewHolder(view, listener);
  }
}
