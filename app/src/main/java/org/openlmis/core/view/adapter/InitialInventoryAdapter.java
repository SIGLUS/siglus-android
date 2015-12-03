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

import org.openlmis.core.R;
import org.openlmis.core.view.holder.InitialInventoryViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.List;

public class InitialInventoryAdapter extends InventoryListAdapter<InitialInventoryViewHolder> {

    public InitialInventoryAdapter(List<StockCardViewModel> data) {
        super(data);
    }

    @Override
    public void onBindViewHolder(final InitialInventoryViewHolder holder, final int position) {
        final StockCardViewModel viewModel = currentList.get(position);
        holder.populate(viewModel, queryKeyWord);
    }


    @Override
    public InitialInventoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InitialInventoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false));
    }
}
