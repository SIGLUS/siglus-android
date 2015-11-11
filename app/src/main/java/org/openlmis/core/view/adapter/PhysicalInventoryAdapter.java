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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.PhysicalInventoryViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.List;


public class PhysicalInventoryAdapter extends InventoryListAdapter<RecyclerView.ViewHolder> implements FilterableAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private View footView;

    public PhysicalInventoryAdapter(Context context, List<StockCardViewModel> data) {
        super(context, data);
    }

    public PhysicalInventoryAdapter(Context context, List<StockCardViewModel> data, View footView) {
        this(context, data);
        this.footView = footView;

        if (!(footView instanceof EditText)) {
            footView.setFocusable(false);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = inflater.inflate(R.layout.item_physical_inventory, parent, false);
            return new PhysicalInventoryViewHolder(view);
        } else {
            return new VHFooter(footView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (position >= currentList.size()) {
            return;
        }
        PhysicalInventoryViewHolder holder = (PhysicalInventoryViewHolder) viewHolder;
        final StockCardViewModel viewModel = currentList.get(position);

        holder.populate(viewModel);
    }

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
        return position == currentList.size();
    }

}
