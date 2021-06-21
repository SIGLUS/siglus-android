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
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.fragment.BaseFragment;
import org.openlmis.core.view.holder.RapidTestReportViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

public class RapidTestReportAdapter extends RecyclerView.Adapter<RapidTestReportViewHolder> {

  private final Context context;
  private final List<RapidTestReportViewModel> viewModels;
  private BaseFragment container;

  public RapidTestReportAdapter(BaseFragment container, List<RapidTestReportViewModel> viewModels) {
    this.context = container.requireContext();
    this.viewModels = viewModels;
    this.container = container;
  }

  @Override
  public RapidTestReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == RapidTestReportViewModel.Status.COMPLETED.getViewType()) {
      return new RapidTestReportViewHolder(
          LayoutInflater.from(context).inflate(R.layout.item_report_no_button, parent, false), container);
    }
    return new RapidTestReportViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_rapid_test_report, parent, false), container);
  }

  @Override
  public void onBindViewHolder(RapidTestReportViewHolder holder, int position) {
    holder.populate(viewModels.get(position));
  }

  @Override
  public int getItemCount() {
    return viewModels.size();
  }

  @Override
  public int getItemViewType(int position) {
    return viewModels.get(position).getStatus().getViewType();
  }
}
