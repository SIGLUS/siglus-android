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
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.AddDrugsToVIAViewHolder;
import org.openlmis.core.view.viewmodel.AddDrugsToViaInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class AddDrugsToVIAAdapter extends InventoryListAdapter<AddDrugsToVIAViewHolder> implements
    FilterableAdapter {

  public AddDrugsToVIAAdapter(List<InventoryViewModel> inventoryViewModels) {
    super(inventoryViewModels);
  }

  @Override
  public AddDrugsToVIAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AddDrugsToVIAViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_select_product_in_via, parent, false));
  }

  @Override
  public void onBindViewHolder(AddDrugsToVIAViewHolder holder, int position) {
    holder.populate(queryKeyWord, (AddDrugsToViaInventoryViewModel) filteredList.get(position));
  }
}
