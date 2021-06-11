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

package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.openlmis.core.R;
import org.openlmis.core.view.adapter.LotInfoReviewListAdapter;
import org.openlmis.core.view.adapter.PhysicalInventoryLotMovementAdapter;
import org.openlmis.core.view.holder.PhysicalInventoryWithLotViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.InjectView;

public class PhysicalInventoryLotListView extends BaseLotListView {

  @InjectView(R.id.btn_done)
  ViewGroup btnDone;

  @InjectView(R.id.btn_edit)
  TextView btnEdit;

  @InjectView(R.id.vg_edit_lot_area)
  ViewGroup vgEditLotArea;

  @InjectView(R.id.vg_lot_info_review)
  ViewGroup vgLotInfoReview;

  @InjectView(R.id.rv_lot_info_review)
  RecyclerView rvLotInfoReview;
  private PhysicalInventoryWithLotViewHolder.InventoryItemStatusChangeListener statusChangeListener;

  public PhysicalInventoryLotListView(Context context) {
    super(context);
  }

  public PhysicalInventoryLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void init(Context context) {
    super.init(context);
    btnDone.setOnClickListener(v -> {
      if (validateLotList()) {
        markDone(true);
      }
    });
    btnEdit.setOnClickListener(v -> markDone(false));
  }

  public void initLotListView(InventoryViewModel viewModel,
      PhysicalInventoryWithLotViewHolder.InventoryItemStatusChangeListener statusChangeListener) {
    this.statusChangeListener = statusChangeListener;
    super.initLotListView(viewModel);
    markDone(((PhysicalInventoryViewModel) viewModel).isDone());
  }

  @Override
  public void initExistingLotListView() {
    existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    FluentIterable.from(viewModel.getExistingLotMovementViewModelList())
        .transform(lotMovementViewModel -> {
          lotMovementViewModel.setFrom(((PhysicalInventoryViewModel) viewModel).getFrom());
          return lotMovementViewModel;
        }).toList();
    existingLotMovementAdapter = new PhysicalInventoryLotMovementAdapter(
        viewModel.getExistingLotMovementViewModelList());
    existingLotListView.setAdapter(existingLotMovementAdapter);
  }

  @Override
  public void initNewLotListView() {
    newLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    FluentIterable.from(viewModel.getNewLotMovementViewModelList())
        .transform(lotMovementViewModel -> {
          lotMovementViewModel.setFrom(((PhysicalInventoryViewModel) viewModel).getFrom());
          return lotMovementViewModel;
        }).toList();
    newLotMovementAdapter = new PhysicalInventoryLotMovementAdapter(
        viewModel.getNewLotMovementViewModelList(),
        viewModel.getProduct().getProductNameWithCodeAndStrength());
    newLotListView.setAdapter(newLotMovementAdapter);
  }

  private void initLotInfoReviewList() {
    LotInfoReviewListAdapter adapter = new LotInfoReviewListAdapter(viewModel);
    rvLotInfoReview.setLayoutManager(new LinearLayoutManager(context));
    rvLotInfoReview.setAdapter(adapter);
  }


  public void markDone(boolean done) {
    ((PhysicalInventoryViewModel) viewModel).setDone(done);
    vgEditLotArea.setVisibility(done ? GONE : VISIBLE);
    vgLotInfoReview.setVisibility(done ? VISIBLE : GONE);
    statusChangeListener.onStatusChange(done);
    if (done) {
      initLotInfoReviewList();
    }
  }

  public boolean validateLotList() {
    int position1 = ((PhysicalInventoryLotMovementAdapter) existingLotMovementAdapter)
        .validateLotNonEmptyQuantity();
    if (position1 >= 0) {
      existingLotListView.scrollToPosition(position1);
      return false;
    }
    int position2 = ((PhysicalInventoryLotMovementAdapter) newLotMovementAdapter)
        .validateLotPositiveQuantity();
    if (position2 >= 0) {
      newLotListView.scrollToPosition(position2);
      return false;
    }
    return true;
  }

  @Override
  protected void inflateLayout(Context context) {
    inflate(context, R.layout.view_lot_list_physical_inventory, this);
  }
}
