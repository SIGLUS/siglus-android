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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.RapidTestReport4ColumnsGridViewHolder;
import org.openlmis.core.view.holder.RapidTestReportGridViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;

public class RapidTestReportGridAdapter extends
    RecyclerView.Adapter<RapidTestReportGridViewHolder> {

  public static final int FOUR_COLUMN_VIEW_TYPE = 1;
  
  Context context;
  private final boolean editable;
  List<RapidTestFormGridViewModel> viewModels;
  private final RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener;
  private final int itemWidth;
  private final long baseItemWidth;

  public RapidTestReportGridAdapter(List<RapidTestFormGridViewModel> viewModels, Context context,
      boolean editable,
      RapidTestReportGridViewHolder.QuantityChangeListener quantityChangeListener) {
    baseItemWidth = (long) (context.getResources().getDimension(R.dimen.rapid_view_width) / 25);
    itemWidth = (int) (3 * baseItemWidth);
    this.viewModels = viewModels;
    this.context = context;
    this.editable = editable;
    this.quantityChangeListener = quantityChangeListener;
  }

  @Override
  public int getItemViewType(int position) {
    if (viewModels.size() > position && isDuoTest(viewModels.get(position))) {
      return FOUR_COLUMN_VIEW_TYPE;
    }
    return super.getItemViewType(position);
  }

  @NonNull
  @Override
  public RapidTestReportGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (FOUR_COLUMN_VIEW_TYPE == viewType) {
      return new RapidTestReport4ColumnsGridViewHolder(initialize4ColumnItemView(parent));
    }

    return new RapidTestReportGridViewHolder(initializeNormalItemView(parent));
  }

  @NonNull
  private View initialize4ColumnItemView(ViewGroup parent) {
    View itemView;

    if (editable) {
      itemView = LayoutInflater.from(context)
          .inflate(R.layout.item_rapid_test_report_four_columns_grid, parent, false);
    } else {
      itemView = LayoutInflater.from(context)
          .inflate(R.layout.item_rapid_test_report_four_columns_grid_total, parent, false);
    }
    itemView.getLayoutParams().width = (int) (4 * baseItemWidth);

    return itemView;
  }

  @NonNull
  private View initializeNormalItemView(ViewGroup parent) {
    View itemView;
    
    if (editable) {
      itemView = LayoutInflater.from(context)
          .inflate(R.layout.item_rapid_test_report_grid, parent, false);
    } else {
      itemView = LayoutInflater.from(context)
          .inflate(R.layout.item_rapid_test_report_grid_total, parent, false);
    }
    itemView.getLayoutParams().width = itemWidth;
    
    return itemView;
  }

  @Override
  public void onBindViewHolder(RapidTestReportGridViewHolder holder, int position) {
    holder.populate(viewModels.get(position), editable, quantityChangeListener);
  }

  private boolean isDuoTest(RapidTestFormGridViewModel viewModel) {
    return viewModel != null && viewModel.isDuoTest();
  }

  @Override
  public int getItemCount() {
    return viewModels.size();
  }
}
