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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.view.adapter.BulkEntriesAdapter;
import org.openlmis.core.view.adapter.BulkEntriesLotMovementAdapter;
import org.openlmis.core.view.adapter.BulkLotInfoReviewListAdapter;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.InjectView;


public class BulkEntriesLotListView extends BaseLotListView {

  @InjectView(R.id.rv_lots)
  ViewGroup rvLots;

  @InjectView(R.id.ly_action_panel)
  ViewGroup actionPanel;

  @InjectView(R.id.vg_lot_info_review)
  ViewGroup vgLotInfoReview;

  @InjectView(R.id.rv_lot_info_review)
  RecyclerView rvLotInfoReview;

  @InjectView(R.id.btn_verify)
  TextView btnVerify;

  @InjectView(R.id.btn_edit)
  TextView btnEdit;

  private BulkEntriesLotMovementAdapter newBulkEntriesLotMovementAdapter;

  private BulkEntriesAdapter bulkEntriesAdapter;

  public BulkEntriesLotListView(Context context) {
    super(context);
  }

  public BulkEntriesLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void init(Context context) {
    super.init(context);
    btnVerify.setOnClickListener(v -> {
      markDone(true);
    });
    btnEdit.setOnClickListener(v -> {
      markDone(false);
    });
  }

  public void initLotListView(BulkEntriesViewModel bulkEntriesViewModel, BulkEntriesAdapter bulkEntriesAdapter) {
    super.initLotListView(bulkEntriesViewModel);
    this.bulkEntriesAdapter = bulkEntriesAdapter;
  }

  @Override
  public void initExistingLotListView() {
    existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    BulkEntriesLotMovementAdapter existingBulkEntriesLotMovementAdapter = new BulkEntriesLotMovementAdapter(
        viewModel.getExistingLotMovementViewModelList(), getMovementReasonDescriptionList(),
        viewModel.getProduct().getPrimaryName());
    existingLotListView.setAdapter(existingBulkEntriesLotMovementAdapter);
    existingLotListView
        .addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
  }

  @Override
  public void initNewLotListView() {
    newLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    newBulkEntriesLotMovementAdapter = new BulkEntriesLotMovementAdapter(
        viewModel.getNewLotMovementViewModelList(), getMovementReasonDescriptionList(),
        viewModel.getProduct().getPrimaryName());
    newLotListView.setAdapter(newBulkEntriesLotMovementAdapter);
    newLotListView
        .addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
  }

  @Override
  public void refreshNewLotList() {
    newBulkEntriesLotMovementAdapter.notifyDataSetChanged();
  }

  @Override
  protected void inflateLayout(Context context) {
    inflate(context, R.layout.view_lot_list_bulk_entries, this);
  }

  private String[] getMovementReasonDescriptionList() {
    String[] reasonDescriptionList;
    List<MovementReason> movementReasons = MovementReasonManager
        .getInstance().buildReasonListForMovementType(MovementType.RECEIVE);
    reasonDescriptionList = FluentIterable.from(movementReasons)
        .transform(MovementReason::getDescription).toArray(String.class);
    return reasonDescriptionList;
  }

  public void markDone(boolean done) {
    ((BulkEntriesViewModel) viewModel).setDone(true);
    rvLots.setVisibility(done ? GONE : VISIBLE);
    actionPanel.setVisibility(done ? GONE : VISIBLE);
    vgLotInfoReview.setVisibility(done ? VISIBLE : GONE);
    bulkEntriesAdapter.notifyDataSetChanged();
    if (done) {
      initLotInfoReviewList();
    }
  }

  private void initLotInfoReviewList() {
    BulkLotInfoReviewListAdapter adapter = new BulkLotInfoReviewListAdapter(viewModel);
    rvLotInfoReview.setLayoutManager(new LinearLayoutManager(context));
    rvLotInfoReview.setAdapter(adapter);
  }
}
