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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
import org.openlmis.core.view.widget.SelectPeriodCardView;

public class SelectPeriodAdapter extends BaseAdapter {

  private final List<SelectInventoryViewModel> list = new ArrayList<>();

  @Override
  public int getCount() {
    return list.size();
  }

  @Override
  public SelectInventoryViewModel getItem(int position) {
    return list.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    SelectPeriodCardView inventoryCardView;

    if (convertView == null) {
      inventoryCardView = (SelectPeriodCardView) LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_inventory_date, null, false);
    } else {
      inventoryCardView = (SelectPeriodCardView) convertView;
    }

    inventoryCardView.populate(getItem(position));
    return inventoryCardView;
  }

  public void refreshDate(List<SelectInventoryViewModel> inventories) {
    list.clear();
    list.addAll(inventories);
    notifyDataSetChanged();
  }
}
