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

package org.openlmis.core.view.holder;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.adapter.RapidTestReportGridAdapter;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;
import roboguice.inject.InjectView;

public class RapidTestReportRowViewHolder extends BaseViewHolder {

  private static final String TAG = RapidTestReportRowViewHolder.class.getSimpleName();

  @InjectView(R.id.rv_rapid_report_grid_item_list)
  RecyclerView rvRapidReportGridListView;

  RapidTestReportGridAdapter adapter;
  List<RapidTestFormGridViewModel> rapidTestFormGridViewModelList;

  public RapidTestReportRowViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(RapidTestFormItemViewModel viewModel, Boolean editable,
      RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
    rapidTestFormGridViewModelList = viewModel.getRapidTestFormGridViewModelList();
    adapter = new RapidTestReportGridAdapter(rapidTestFormGridViewModelList, context,
        !isTotal(viewModel) && editable, isTotal(viewModel) ? null : quantityChangeListener);
    rvRapidReportGridListView
        .setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    rvRapidReportGridListView.setAdapter(adapter);
  }

  /**
   * don`t use notifyDataChange update value, it will make item relayout and recyclerView scroll unexpected pixel
   */
  public void updateRowValue() {
    for (int i = 0; i < adapter.getItemCount(); i++) {
      final RecyclerView.ViewHolder viewHolder = rvRapidReportGridListView
          .findViewHolderForAdapterPosition(i);
      final RapidTestFormGridViewModel rapidTestFormGridViewModel = rapidTestFormGridViewModelList
          .get(i);
      if (viewHolder == null || rapidTestFormGridViewModel == null) {
        return;
      }
      ((RapidTestReportGridViewHolder) viewHolder).populateData(rapidTestFormGridViewModel);
    }
  }

  public boolean isTotal(RapidTestFormItemViewModel viewModel) {
    return viewModel.getIssueReason().getDescription()
        .equals(LMISApp.getInstance().getString(R.string.total));
  }

  public boolean isAPEs(RapidTestFormItemViewModel viewModel) {
    return viewModel.getIssueReason().getDescription()
        .equals(LMISApp.getInstance().getString(R.string.ape));
  }
}
