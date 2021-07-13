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
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.view.viewmodel.InventoryViewModel;


public abstract class InventoryListAdapterWithBottomBtn extends
    InventoryListAdapter<RecyclerView.ViewHolder> implements FilterableAdapter {

  private static final int TYPE_ITEM = 0;
  private static final int TYPE_FOOTER = 1;

  protected InventoryListAdapterWithBottomBtn(List<InventoryViewModel> data) {
    super(data);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    if (position >= filteredList.size()) {
      return;
    }
    populate(viewHolder, position);
  }

  protected abstract void populate(RecyclerView.ViewHolder viewHolder, int position);

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == TYPE_ITEM) {
      return onCreateItemViewHolder(parent);
    } else {
      return onCreateFooterView(parent);
    }
  }

  protected abstract RecyclerView.ViewHolder onCreateFooterView(ViewGroup parent);

  public abstract RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent);

  public static class VHFooter extends RecyclerView.ViewHolder {

    public VHFooter(View itemView) {
      super(itemView);
    }
  }

  @Override
  public int getItemCount() {
    int itemCount = super.getItemCount();
    return itemCount == 0 ? itemCount : itemCount + 1;
  }

  @Override
  public int getItemViewType(int position) {
    return isPositionFooter(position) ? TYPE_FOOTER : TYPE_ITEM;
  }

  private boolean isPositionFooter(int position) {
    return position == filteredList.size();
  }

}
