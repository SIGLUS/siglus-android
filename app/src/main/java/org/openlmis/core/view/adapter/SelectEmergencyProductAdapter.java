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

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.SelectEmergencyProductsViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class SelectEmergencyProductAdapter extends
    InventoryListAdapter<SelectEmergencyProductsViewHolder> implements FilterableAdapter {

  public static final int MAX_CHECKED_LIMIT = 10;

  public SelectEmergencyProductAdapter(List<InventoryViewModel> data) {
    super(data);
  }

  public List<InventoryViewModel> getCheckedProducts() {
    return from(data).filter(viewModel -> viewModel.isChecked()).toList();
  }

  @Override
  public SelectEmergencyProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new SelectEmergencyProductsViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_select_product, parent, false));
  }

  @Override
  public void onBindViewHolder(SelectEmergencyProductsViewHolder holder, int position) {
    holder.populate(this, queryKeyWord, filteredList.get(position));
  }

  public boolean isReachLimit() {
    return getCheckedProducts().size() == MAX_CHECKED_LIMIT;
  }
}
