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
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.BulkEntriesLotMovementViewHolder;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

public class BulkEntriesLotMovementAdapter extends
    RecyclerView.Adapter<BulkEntriesLotMovementViewHolder> {

  @Getter
  protected final List<LotMovementViewModel> lotList;

  @Getter
  private final String productName;

  private final BulkEntriesViewModel bulkEntriesViewModel;

  private final BulkEntriesAdapter bulkEntriesAdapter;

  private final String[] movementReasons;

  private BulkEntriesLotMovementViewHolder.AmountChangeListener amountChangeListener;


  public BulkEntriesLotMovementAdapter(
      List<LotMovementViewModel> lotList, String[] reasonDescriptionList,
      BulkEntriesViewModel bulkEntriesViewModel, BulkEntriesAdapter bulkEntriesAdapter) {
    this.lotList = lotList;
    this.movementReasons = reasonDescriptionList;
    this.productName = bulkEntriesViewModel.getProduct().getPrimaryName();
    this.bulkEntriesViewModel = bulkEntriesViewModel;
    this.bulkEntriesAdapter = bulkEntriesAdapter;
  }

  @NonNull
  @Override
  public BulkEntriesLotMovementViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    return new BulkEntriesLotMovementViewHolder(LayoutInflater.from(parent.getContext()).inflate(
        R.layout.item_bulk_entries_lots_info, parent, false), movementReasons);
  }

  @Override
  public void onBindViewHolder(@NonNull BulkEntriesLotMovementViewHolder holder, int position) {
    holder.populate(lotList.get(position), this);
    holder.setMovementChangeListener(amountChangeListener);
  }

  @Override
  public int getItemCount() {
    return lotList.size();
  }

  public void setMovementChangeListener(
      BulkEntriesLotMovementViewHolder.AmountChangeListener amountChangedListener) {
    this.amountChangeListener = amountChangedListener;
  }

  public void remove(LotMovementViewModel viewModel) {
    lotList.remove(viewModel);
    if (lotList.isEmpty()) {
      bulkEntriesViewModel.setValid(bulkEntriesViewModel.validate());
      bulkEntriesAdapter.notifyDataSetChanged();
    }
    this.notifyDataSetChanged();
  }

  public boolean validateExistingLots() {
    boolean valid = true;
    for (LotMovementViewModel lotMovementViewModel : bulkEntriesViewModel.getExistingLotMovementViewModelList()) {
      if (!StringUtils.isBlank(lotMovementViewModel.getQuantity())) {
        boolean flag = lotMovementViewModel.validateLot();
        if (!flag) {
          valid = false;
        }
      }
    }
    this.notifyDataSetChanged();
    return valid;
  }

  public int validateNewAddedLots() {
    List<LotMovementViewModel> newLots = bulkEntriesViewModel.getNewLotMovementViewModelList();
    int position = -1;
    for (int i = 0; i < newLots.size(); i++) {
      if (!newLots.get(i).validateLot() && position < 0) {
        position = i;
      }
    }
    this.notifyDataSetChanged();
    return position;
  }

}
