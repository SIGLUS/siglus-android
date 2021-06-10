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
import java.util.List;
import lombok.Getter;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.LotMovementViewHolder;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

public class LotMovementAdapter extends RecyclerView.Adapter<LotMovementViewHolder> {

  @Getter
  protected final List<LotMovementViewModel> lotList;

  @Getter
  private final String productName;
  private MovementChangedListener movementChangedListener;
  private MovementChangedListenerWithStatus movementChangedListenerWithStatus;

  public LotMovementAdapter(List<LotMovementViewModel> data) {
    this.lotList = data;
    productName = null;
  }

  public LotMovementAdapter(List<LotMovementViewModel> data, String productName) {
    this.lotList = data;
    this.productName = productName;
  }

  @Override
  public LotMovementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_lots_info, parent, false);
    return new LotMovementViewHolder(view);
  }

  @Override
  public void onBindViewHolder(LotMovementViewHolder holder, int position) {
    final LotMovementViewModel viewModel = lotList.get(position);

    holder.setMovementChangeListener(movementChangedListener);
    holder.setMovementChangedLisenerWithStatus(movementChangedListenerWithStatus);
    holder.populate(viewModel, this);
  }

  @Override
  public int getItemCount() {
    return lotList.size();
  }

  public void remove(LotMovementViewModel viewModel) {
    lotList.remove(viewModel);
    if (movementChangedListener != null) {
      movementChangedListener.movementChange();
    }
    this.notifyDataSetChanged();
  }

  public void setMovementChangeListener(MovementChangedListener movementChangedListener) {
    this.movementChangedListener = movementChangedListener;
  }

  public void setMovementChangedListenerWithStatus(
      MovementChangedListenerWithStatus movementChangedListenerWithStatus) {
    this.movementChangedListenerWithStatus = movementChangedListenerWithStatus;
  }

  public interface MovementChangedListener {

    void movementChange();
  }

  public interface MovementChangedListenerWithStatus {

    void movementChange(String amount);
  }
}
