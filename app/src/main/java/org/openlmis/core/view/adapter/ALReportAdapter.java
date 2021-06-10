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
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.view.holder.ALReportViewHolder;
import org.openlmis.core.view.viewmodel.ALReportItemViewModel;
import org.openlmis.core.view.viewmodel.ALReportViewModel;

public class ALReportAdapter extends RecyclerView.Adapter<ALReportViewHolder> {

  private ALReportViewModel alReportViewModel;
  private final ALReportViewHolder.QuantityChangeListener quantityChangeListener;

  public ALReportAdapter(ALReportViewHolder.QuantityChangeListener quantityChangeListener) {
    alReportViewModel = new ALReportViewModel();
    this.quantityChangeListener = quantityChangeListener;
  }

  @Override
  public ALReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_al_report_row, parent, false);
    return new ALReportViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(ALReportViewHolder holder, int position) {
    ALReportItemViewModel viewModel = alReportViewModel.getItemViewModelList().get(position);
    holder.populate(viewModel, quantityChangeListener, getFormStatus(alReportViewModel));
  }

  @Override
  public int getItemCount() {
    return alReportViewModel.getItemViewModelList().size();
  }

  private boolean getFormStatus(ALReportViewModel alReportViewModel) {
    return alReportViewModel != null
        && alReportViewModel.getForm() != null
        && (alReportViewModel.getForm().getStatus() == RnRForm.STATUS.AUTHORIZED
        || alReportViewModel.getForm().getStatus() == RnRForm.STATUS.SUBMITTED);
  }

  public void updateTotal() {
    notifyItemChanged(getItemCount() - 1);
  }

  public void updateTip() {
    notifyItemChanged(0);
    notifyItemChanged(1);

  }

  public void refresh(ALReportViewModel viewModel) {
    alReportViewModel = viewModel;
    notifyDataSetChanged();
  }
}
