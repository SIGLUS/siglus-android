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
import lombok.Getter;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.BulkEntriesViewHolder;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;


public class BulkEntriesAdapter extends RecyclerView.Adapter<BulkEntriesViewHolder> {

  @Getter
  private final List<BulkEntriesViewModel> models;

  public BulkEntriesAdapter(List<BulkEntriesViewModel> allAddedBulkEntriesViewModels) {
    this.models = allAddedBulkEntriesViewModels;

  }

  @NonNull
  @Override
  public BulkEntriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new BulkEntriesViewHolder(LayoutInflater.from(parent.getContext()).inflate(
        (R.layout.item_bulk_entries),parent,false));
  }

  @Override
  public void onBindViewHolder(@NonNull BulkEntriesViewHolder holder, int position) {
    holder.populate(models.get(position));
  }

  @Override
  public int getItemCount() {
    return models.size();
  }

  public void refresh() {
    this.notifyDataSetChanged();
  }
}
