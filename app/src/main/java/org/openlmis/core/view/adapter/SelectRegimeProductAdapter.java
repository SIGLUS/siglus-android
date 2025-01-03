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
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.SelectRegimeProductsViewHolder;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;

public class SelectRegimeProductAdapter extends
    RecyclerView.Adapter<SelectRegimeProductsViewHolder> {

  private final List<RegimeProductViewModel> products;

  public SelectRegimeProductAdapter(List<RegimeProductViewModel> products) {
    this.products = products;
  }

  @Override
  public SelectRegimeProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new SelectRegimeProductsViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_select_product_regime, parent, false));
  }

  @Override
  public void onBindViewHolder(SelectRegimeProductsViewHolder holder, int position) {
    RegimeProductViewModel product = products.get(position);
    holder.populate(product);
  }

  @Override
  public int getItemCount() {
    return products.size();
  }
}
