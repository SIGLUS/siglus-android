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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.view.holder.RequisitionProductViewHolder;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;

public class RequisitionProductAdapter extends BaseAdapter {

  private final Context context;

  private final VIARequisitionPresenter presenter;

  public RequisitionProductAdapter(Context context, VIARequisitionPresenter presenter) {
    this.context = context;
    this.presenter = presenter;
  }

  @Override
  public int getCount() {
    return data() == null ? 0 : data().size();
  }

  @Override
  public RequisitionFormItemViewModel getItem(int position) {
    return data() == null && data().size() == 0 ? null : data().get(position);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    RequisitionProductViewHolder viewHolder;

    if (convertView == null) {
      convertView = LayoutInflater.from(context)
          .inflate(R.layout.item_requisition_body_left, parent, false);
      viewHolder = new RequisitionProductViewHolder(convertView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (RequisitionProductViewHolder) convertView.getTag();
    }
    viewHolder.populate(getItem(position), presenter, context);
    return convertView;
  }

  private List<RequisitionFormItemViewModel> data() {
    return presenter.getRequisitionFormItemViewModels();
  }
}
