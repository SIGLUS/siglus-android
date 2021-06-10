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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import org.openlmis.core.view.widget.SelectUnpackNumCardView;

public class UnpackNumAdapter extends BaseAdapter {

  private final long kitSOH;
  private final String kitType;
  private final Context context;

  public UnpackNumAdapter(Context context, long kitSOH, String kitType) {
    this.context = context;
    this.kitSOH = kitSOH;
    this.kitType = kitType;
  }

  @Override
  public int getCount() {
    return (int) kitSOH;
  }

  @Override
  public Integer getItem(int position) {
    return position + 1;
  }

  @Override
  public long getItemId(int position) {
    return position + 1L;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    SelectUnpackNumCardView cardView;

    if (convertView == null) {
      cardView = new SelectUnpackNumCardView(context);
    } else {
      cardView = (SelectUnpackNumCardView) convertView;
    }

    cardView.populate(getItem(position), kitType);
    return cardView;
  }
}
