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
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.model.Program;
import org.openlmis.core.view.holder.BaseViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryWithLotViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class BulkInitialInventoryAdapter extends InventoryListAdapter<BaseViewHolder> {

  private final RemoveNonBasicProduct removeNonBasicProductListener;
  private final InventoryItemStatusChangeListener refreshCompleteCountListener;

  public static final int ITEM_BASIC = 1;
  public static final int ITEM_NO_BASIC = ITEM_BASIC + 1;
  public static final int ITEM_BASIC_HEADER = ITEM_NO_BASIC + 1;
  public static final int ITEM_NON_BASIC_HEADER = ITEM_BASIC_HEADER + 1;

  public BulkInitialInventoryAdapter(List<InventoryViewModel> data,
      RemoveNonBasicProduct removeNonBasicProductListener,
      InventoryItemStatusChangeListener refreshCompleteCountListener) {
    super(data);
    this.removeNonBasicProductListener = removeNonBasicProductListener;
    this.refreshCompleteCountListener = refreshCompleteCountListener;
  }

  protected void populate(RecyclerView.ViewHolder viewHolder, int position) {
    final InventoryViewModel viewModel = filteredList.get(position);
    BulkInitialInventoryWithLotViewHolder holder = (BulkInitialInventoryWithLotViewHolder) viewHolder;
    holder.populate((BulkInitialInventoryViewModel) viewModel, queryKeyWord,
        refreshCompleteCountListener, removeNonBasicProductListener);
  }

  @Override
  public int getItemViewType(int position) {
    return filteredList.get(position).getViewType();
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

  public int validateAllForCompletedClick() {
    validateFailedProgram.clear();
    int position = -1;
    for (int i = 0; i < data.size(); i++) {
      final InventoryViewModel viewModel = data.get(i);
      if (viewModel.validate()
          || !(viewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_BASIC_HEADER
          || viewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER)) {
        continue;
      }
      final Program program = viewModel.getProgram();
      if (program != null && !validateFailedProgram.contains(program)) {
        validateFailedProgram.add(program);
      }
      if (position == -1) {
        position = i;
      }
    }
    this.notifyDataSetChanged();
    return position;
  }

  @Override
  @NonNull
  public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == ITEM_BASIC_HEADER) {
      return new BulkInitialInventoryWithLotViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_bulk_initial_inventory_header, parent, false));
    } else if (viewType == ITEM_NON_BASIC_HEADER) {
      return new BulkInitialInventoryWithLotViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_bulk_initial_inventory_non_basic_header, parent, false));
    } else {
      return new BulkInitialInventoryWithLotViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.bulk_item_initial_inventory, parent, false));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    if (position >= filteredList.size()) {
      return;
    }
    populate(holder, position);
  }

  public interface RemoveNonBasicProduct {

    void removeNoneBasicProduct(BulkInitialInventoryViewModel viewModel);
  }
}
