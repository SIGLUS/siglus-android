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
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.RapidTestReportGridViewHolder;
import org.openlmis.core.view.holder.RapidTestReportObservationRowViewHolder;
import org.openlmis.core.view.holder.RapidTestReportRowViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

public class RapidTestReportRowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int ITEM_VIEW = 1;
  private static final int OBSERVATION_VIEW = 2;
  private List<RapidTestFormItemViewModel> serviceLists;
  private RapidTestReportViewModel rapidTestReportViewModel;
  private RapidTestReportRowViewHolder totalViewHolder;
  private RapidTestReportRowViewHolder apeViewHolder;

  @Setter
  private Boolean editable = true;
  private final RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener;

  public RapidTestReportRowAdapter(
      RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
    this.quantityChangeListener = quantityChangeListener;
    this.serviceLists = new ArrayList<>();
  }

  @Override
  public int getItemViewType(int position) {
    if (position == serviceLists.size()) {
      return OBSERVATION_VIEW;
    }
    return ITEM_VIEW;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == ITEM_VIEW) {
      View itemView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_rapid_test_report_row, parent, false);
      return new RapidTestReportRowViewHolder(itemView);
    } else {
      View itemView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_rapid_test_report_observation_row, parent, false);
      return new RapidTestReportObservationRowViewHolder(itemView);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    switch (holder.getItemViewType()) {
      case ITEM_VIEW:
        RapidTestReportRowViewHolder viewHolder = (RapidTestReportRowViewHolder) holder;
        final RapidTestFormItemViewModel viewModel = serviceLists.get(position);
        if (viewHolder.isTotal(viewModel)) {
          totalViewHolder = viewHolder;
        }
        if (viewHolder.isAPEs(viewModel)) {
          apeViewHolder = viewHolder;
        }
        viewHolder.setIsRecyclable(false);
        viewHolder.populate(viewModel, editable, quantityChangeListener);
        break;
      case OBSERVATION_VIEW:
        RapidTestReportObservationRowViewHolder observationRowViewHolder =
            (RapidTestReportObservationRowViewHolder) holder;
        observationRowViewHolder.setIsRecyclable(false);
        observationRowViewHolder.populate(rapidTestReportViewModel);
        break;
      default:
        break;

    }
  }

  @Override
  public int getItemCount() {
    return serviceLists.isEmpty() ? 0 : serviceLists.size() + 1;
  }

  public void refresh(RapidTestReportViewModel viewModel) {
    rapidTestReportViewModel = viewModel;
    serviceLists = rapidTestReportViewModel.getItemViewModelList();
    this.editable = viewModel.isEditable();
    notifyDataSetChanged();
  }

  public void updateRowValue() {
    if (totalViewHolder != null) {
      totalViewHolder.updateRowValue();
    }
    if (apeViewHolder != null) {
      apeViewHolder.updateRowValue();
    }
  }

}
