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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.view.holder.RequisitionFormViewHolder;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;

public class RequisitionFormAdapter extends BaseAdapter {

  private final Context context;

  private final VIARequisitionPresenter presenter;

  private Status status = Status.AUTHORIZED;

  public RequisitionFormAdapter(Context context, VIARequisitionPresenter presenter) {
    this.context = context;
    this.presenter = presenter;
  }

  @Override
  public int getCount() {
    return data() == null ? 0 : data().size();
  }

  @Override
  public RequisitionFormItemViewModel getItem(int position) {
    return data() == null ? null : data().get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    RequisitionFormViewHolder viewHolder;

    if (convertView == null) {
      convertView = LayoutInflater.from(context)
          .inflate(R.layout.item_requisition_body, parent, false);
      viewHolder = new RequisitionFormViewHolder(convertView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (RequisitionFormViewHolder) convertView.getTag();
    }

    View currentFocus = ((Activity) context).getCurrentFocus();
    if (currentFocus != null) {
      currentFocus.clearFocus();
    }

    final RequisitionFormItemViewModel entry = getItem(position);
    viewHolder.populate(entry, status);
    viewHolder.setBackgroundColor(position);

    return convertView;
  }

  public void updateStatus(Status status) {
    this.status = status;
    this.notifyDataSetChanged();
  }

  private List<RequisitionFormItemViewModel> data() {
    return presenter.getRequisitionFormItemViewModels();
  }
}
