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
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.viethoa.RecyclerViewFastScroller;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.activity.PhysicalInventoryActivity;
import org.openlmis.core.view.holder.PhysicalInventoryWithLotViewHolder;
import org.openlmis.core.view.holder.PhysicalInventoryWithLotViewHolder.InventoryItemStatusChangeListener;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;

@SuppressWarnings("PMD")
public class PhysicalInventoryAdapter extends InventoryListAdapter<PhysicalInventoryWithLotViewHolder> implements
    FilterableAdapter, RecyclerViewFastScroller.BubbleTextGetter {

  private final InventoryItemStatusChangeListener refreshCompleteCountListener;

  public PhysicalInventoryAdapter(List<InventoryViewModel> data, InventoryItemStatusChangeListener refreshListener) {
    super(data);
    this.refreshCompleteCountListener = refreshListener;
  }

  @NonNull
  @Override
  public PhysicalInventoryWithLotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new PhysicalInventoryWithLotViewHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_physical_inventory, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull PhysicalInventoryWithLotViewHolder holder, int position) {
    final InventoryViewModel viewModel = filteredList.get(position);
    holder.populate((PhysicalInventoryViewModel) viewModel, queryKeyWord, refreshCompleteCountListener);
  }

  @Override
  public String getTextToShowInBubble(int position) {
    if (position < 0 || position >= data.size()) {
      return null;
    }

    String name = data.get(position).getProductName();
    if (name == null || name.length() < 1) {
      return null;
    }

    return data.get(position).getProductName().substring(0, 1);
  }

  public boolean isHasDataChanged() {
    List<InventoryViewModel> data = getData();
    for (InventoryViewModel model : data) {
      if (model.isDataChanged()) {
        return true;
      }
    }
    return false;
  }

  public int validateAllForCompletedClick(String from) {
    int position = -1;
    for (int i = 0; i < data.size(); i++) {
      ((PhysicalInventoryViewModel) data.get(i)).setFrom(from);
      if (!data.get(i).validate()) {
        if (position == -1 || i < position) {
          position = i;
        }
      }
    }

    this.notifyDataSetChanged();
    return position;
  }

  @Override
  public int validateAll() {
    int position = -1;
    for (int i = 0; i < data.size(); i++) {
      ((PhysicalInventoryViewModel) data.get(i))
          .setFrom(PhysicalInventoryActivity.KEY_FROM_PHYSICAL_COMPLETED);
      if (!data.get(i).validate()) {
        if (position == -1 || i < position) {
          position = i;
        }
      }
    }

    this.notifyDataSetChanged();
    return position;
  }
}
