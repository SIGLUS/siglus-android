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
import android.view.LayoutInflater;


import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.List;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public abstract class InventoryListAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements FilterableAdapter {

    LayoutInflater inflater;
    Context context;
    List<StockCardViewModel> data;
    List<StockCardViewModel> currentList;

    public InventoryListAdapter(Context context, List<StockCardViewModel> data) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.data = data;
        currentList = this.data;
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    public List<StockCardViewModel> getData() {
        return this.data;
    }


    @Override
    public void filter(final String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            this.currentList = data;
            this.notifyDataSetChanged();
            return;
        }

        this.currentList = from(data).filter(new Predicate<StockCardViewModel>() {
            @Override
            public boolean apply(StockCardViewModel stockCardViewModel) {
                return stockCardViewModel.getProductName().toLowerCase().contains(keyword.toLowerCase())
                        || stockCardViewModel.getFnm().toLowerCase().contains(keyword.toLowerCase());
            }
        }).toList();

        this.notifyDataSetChanged();
    }

    public void refreshList(List<StockCardViewModel> data) {
        this.data = data;
        this.currentList.clear();
        this.currentList.addAll(data);
        notifyDataSetChanged();
    }


    @Override
    public int validateAll() {
        int position = -1;
        for (int i = 0; i < data.size(); i++) {
            if (!data.get(i).validate()) {
                position = i;
                break;
            }
        }

        this.notifyDataSetChanged();
        return position;
    }
}
