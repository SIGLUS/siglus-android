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
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.RapidTestReportBodyLeftHeaderViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;

public class RapidTestReportBodyLeftHeaderAdapter extends
    RecyclerView.Adapter<RapidTestReportBodyLeftHeaderViewHolder> {

  private List<RapidTestFormItemViewModel> viewModels;

  public RapidTestReportBodyLeftHeaderAdapter() {
    this.viewModels = new ArrayList<>();
  }

  @Override
  public RapidTestReportBodyLeftHeaderViewHolder onCreateViewHolder(ViewGroup parent,
      int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_rapid_test_report_body_left, parent, false);
    return new RapidTestReportBodyLeftHeaderViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(RapidTestReportBodyLeftHeaderViewHolder holder, int position) {
    if (position == viewModels.size()) {
      holder.setUpObservationLeftHeaderViewHolder();
      return;
    }
    final RapidTestFormItemViewModel viewModel = viewModels.get(position);
    holder.setUpHeader(viewModel);
  }

  @Override
  public int getItemCount() {
    return viewModels.size() + 1;
  }

  public void refresh(List<RapidTestFormItemViewModel> itemViewModelList) {
    viewModels = itemViewModelList;
    notifyDataSetChanged();
  }
}
