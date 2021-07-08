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

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.viethoa.RecyclerViewFastScroller;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.NonBasicProductViewHolder;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;

public class AddNonBasicProductsAdapter extends RecyclerView.Adapter<NonBasicProductViewHolder> implements
    RecyclerViewFastScroller.BubbleTextGetter {

  @Getter
  private final List<NonBasicProductsViewModel> models;

  @Getter
  private final List<NonBasicProductsViewModel> filteredList;

  private String queryKeyword = "";

  public AddNonBasicProductsAdapter(List<NonBasicProductsViewModel> models) {
    filteredList = new ArrayList<>();
    this.models = models;
  }

  @Override
  @NonNull
  public NonBasicProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new NonBasicProductViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_non_basic_product, parent, false));
  }

  @Override
  public void onBindViewHolder(NonBasicProductViewHolder holder, int position) {
    holder.putOnChangedListener(filteredList.get(position));
    holder.populate(filteredList.get(position), queryKeyword);
  }

  @Override
  public int getItemCount() {
    return filteredList.size();
  }

  public void filter(final String keyword) {
    this.queryKeyword = keyword;
    List<NonBasicProductsViewModel> filteredViewModels;
    if (TextUtils.isEmpty(keyword)) {
      filteredViewModels = models;
    } else {
      filteredViewModels = from(models)
          .filter(nonBasicProductsViewModel ->
              nonBasicProductsViewModel.getProduct()
                  .getProductFullName().toLowerCase()
                  .contains(keyword.toLowerCase())).toList();
    }

    filteredList.clear();
    filteredList.addAll(filteredViewModels);
    this.notifyDataSetChanged();
  }


  @Override
  public String getTextToShowInBubble(int position) {
    if (position < 0 || position >= models.size()) {
      return null;
    }

    String name = models.get(position).getProduct().getPrimaryName();
    if (name == null || name.length() < 1) {
      return null;
    }

    return models.get(position).getProduct().getPrimaryName().substring(0, 1);
  }
}
