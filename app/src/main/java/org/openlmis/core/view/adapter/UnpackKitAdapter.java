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
import org.openlmis.core.R;
import org.openlmis.core.view.holder.UnpackKitWithLotViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.UnpackKitInventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import rx.functions.Action1;

public class UnpackKitAdapter extends InventoryListAdapterWithBottomBtn implements FilterableAdapter {

  private final SingleClickButtonListener onClickListener;
  private final Action1<UnpackKitInventoryViewModel> setConfirmNoStockReceivedAction =
      unpackKitInventoryViewModel -> {
        unpackKitInventoryViewModel.setConfirmedNoStockReceived(true);
        unpackKitInventoryViewModel.getNewLotMovementViewModelList().clear();
        UnpackKitAdapter.this.notifyDataSetChanged();
      };

  public UnpackKitAdapter(List<InventoryViewModel> data, SingleClickButtonListener onClickListener) {
    super(data);
    this.onClickListener = onClickListener;
  }

  @Override
  protected void populate(RecyclerView.ViewHolder viewHolder, int position) {
    ((UnpackKitWithLotViewHolder) viewHolder).populate(filteredList.get(position), setConfirmNoStockReceivedAction);
  }

  @Override
  protected VHFooter onCreateFooterView(ViewGroup parent) {
    VHFooter vhFooter = new VHFooter(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.view_complete_btn, parent, false));
    vhFooter.itemView.findViewById(R.id.btn_complete).setOnClickListener(onClickListener);
    return vhFooter;
  }

  @Override
  public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unpack_kit_with_lots, parent, false);
    return new UnpackKitWithLotViewHolder(view);
  }
}
