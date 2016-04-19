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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.PhysicalInventoryViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;


public class PhysicalInventoryAdapter extends InventoryListAdapterWithBottomBtn implements FilterableAdapter {

    private final View.OnClickListener saveClickListener;
    private final View.OnClickListener completeClickListener;

    public PhysicalInventoryAdapter(List<InventoryViewModel> data, View.OnClickListener saveClickListener, View.OnClickListener completeClickListener) {
        super(data);
        this.saveClickListener = saveClickListener;
        this.completeClickListener = completeClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_physical_inventory, parent, false);
        return new PhysicalInventoryViewHolder(view);
    }

    @Override
    protected void populate(RecyclerView.ViewHolder viewHolder, int position) {
        PhysicalInventoryViewHolder holder = (PhysicalInventoryViewHolder) viewHolder;
        final InventoryViewModel viewModel = filteredList.get(position);

        holder.populate(viewModel, queryKeyWord);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateFooterView(ViewGroup parent) {
        VHFooter vhFooter = new VHFooter(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_form_action, parent, false));
        vhFooter.itemView.findViewById(R.id.btn_save).setOnClickListener(saveClickListener);
        vhFooter.itemView.findViewById(R.id.btn_complete).setOnClickListener(completeClickListener);
        return vhFooter;
    }

    public boolean isHasDataChanged() {
        List<InventoryViewModel> data = getData();
        for (InventoryViewModel model : data) {
            if (model.isHasDataChanged()) {
                return true;
            }
        }
        return false;
    }
}
