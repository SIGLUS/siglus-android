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

import static org.openlmis.core.view.adapter.BulkInitialInventoryAdapter.ITEM_BASIC_HEADER;
import static org.openlmis.core.view.adapter.BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.viethoa.RecyclerViewFastScroller;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.Program;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public abstract class InventoryListAdapter<T extends RecyclerView.ViewHolder> extends
    RecyclerView.Adapter<T> implements FilterableAdapter,
    RecyclerViewFastScroller.BubbleTextGetter {

  @Getter
  List<InventoryViewModel> data;

  @Getter
  @Setter
  List<InventoryViewModel> filteredList;

  @Getter
  String queryKeyWord;

  @Getter
  @Nullable
  private Program filterProgram = null;

  @Getter
  protected final List<Program> validateFailedProgram = new ArrayList<>();

  protected InventoryListAdapter(List<InventoryViewModel> data) {
    this.data = data;
    filteredList = new ArrayList<>();
  }

  @Override
  public int getItemCount() {
    return filteredList.size();
  }

  @Override
  public void filter(final String keyword) {
    this.queryKeyWord = keyword;
    FluentIterable<InventoryViewModel> filteredViewModels = from(data);
    if (filterProgram != null) {
      filteredViewModels = filteredViewModels.filter(inventoryViewModel -> {
        if (inventoryViewModel == null) {
          return false;
        }
        boolean isBasicHeader = inventoryViewModel.getViewType() == ITEM_BASIC_HEADER;
        boolean isNonBasicHeader = inventoryViewModel.getViewType() == ITEM_NON_BASIC_HEADER;
        final Program program = inventoryViewModel.getProgram();
        if (isBasicHeader || isNonBasicHeader || program == null) {
          return false;
        }
        String filterProgramProgramCode = filterProgram.getProgramCode();
        String programCode = program.getProgramCode();
        return StringUtils.equals(filterProgramProgramCode, programCode);
      });
    }
    if (!StringUtils.isEmpty(keyword)) {
      filteredViewModels = filteredViewModels.filter(inventoryViewModel -> {
        if (inventoryViewModel == null) {
          return false;
        }
        return inventoryViewModel.getProduct().getProductFullName().toLowerCase().contains(keyword.toLowerCase());
      });
    }
    filteredList.clear();
    filteredList.addAll(filteredViewModels.toList());
    this.notifyDataSetChanged();
  }

  public void refreshList(List<InventoryViewModel> data) {
    this.data = data;
    filter(queryKeyWord);
  }

  public void setFilterProgram(Program filterProgram) {
    this.filterProgram = filterProgram;
    filter(queryKeyWord);
  }

  @Override
  public int validateAll() {
    validateFailedProgram.clear();
    int position = -1;
    for (int i = 0; i < data.size(); i++) {
      final InventoryViewModel viewModel = data.get(i);
      if (viewModel.validate()) {
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

  public void refresh() {
    filter(queryKeyWord);
  }

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
}
