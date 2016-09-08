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
import android.text.TextUtils;

import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public abstract class InventoryListAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements FilterableAdapter {

    @Getter
    List<InventoryViewModel> data;

    @Getter
    List<InventoryViewModel> filteredList = new ArrayList<>();
    String queryKeyWord;

    public InventoryListAdapter(List<InventoryViewModel> data) {
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public void filter(final String keyword) {
        this.queryKeyWord = keyword;

        List<InventoryViewModel> filteredViewModels;

        if (TextUtils.isEmpty(keyword)) {
            filteredViewModels = data;
        } else {
            filteredViewModels = from(data).filter(new Predicate<InventoryViewModel>() {
                @Override
                public boolean apply(InventoryViewModel inventoryViewModel) {
                    return inventoryViewModel.getProduct().getProductFullName().toLowerCase().contains(keyword.toLowerCase());
                }
            }).toList();
        }

        filteredList.clear();
        filteredList.addAll(filteredViewModels);
        this.notifyDataSetChanged();
    }

    public void refreshList(List<InventoryViewModel> data) {
        this.data = data;
        filter(queryKeyWord);
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

    public void refresh() {
        filter(queryKeyWord);
    }
}
