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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.BulkEntriesActivity;
import org.openlmis.core.view.adapter.BulkEntriesAdapter;
import org.openlmis.core.view.adapter.BulkEntriesLotMovementAdapter;
import org.openlmis.core.view.adapter.BulkLotInfoReviewListAdapter;
import org.openlmis.core.view.holder.BulkEntriesLotMovementViewHolder;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel.ValidationType;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.InjectView;


public class BulkEntriesLotListView extends BaseLotListView {

  @InjectView(R.id.rv_lots)
  private ViewGroup rvLots;

  @InjectView(R.id.vg_lot_info_review)
  private ViewGroup vgLotInfoReview;

  @InjectView(R.id.alert_add_positive_lot_amount)
  private ViewGroup alertAddPositiveLotAmount;

  @InjectView(R.id.rv_lot_info_review)
  private RecyclerView rvLotInfoReview;

  @InjectView(R.id.btn_verify)
  private TextView btnVerify;

  @InjectView(R.id.btn_edit)
  private TextView btnEdit;

  private BulkEntriesAdapter bulkEntriesAdapter;

  private BulkEntriesViewModel bulkEntriesViewModel;

  private BulkEntriesLotMovementViewHolder.AmountChangeListener amountChangeListenerFromAlert;

  private BulkEntriesLotMovementViewHolder.AmountChangeListener amountChangeListenerFromTrashcan;

  private MovementStatusListener movementStatusListener;

  private VerifyListener verifyListener;

  public BulkEntriesLotListView(Context context) {
    super(context);
  }

  public BulkEntriesLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void initLotListView(BulkEntriesViewModel bulkEntriesViewModel,
      BulkEntriesAdapter bulkEntriesAdapter,
      BulkEntriesLotMovementViewHolder.AmountChangeListener amountChangeListenerFromTrashcan,
      MovementStatusListener movementStatusListener,
      VerifyListener verifyListener) {
    this.bulkEntriesAdapter = bulkEntriesAdapter;
    this.bulkEntriesViewModel = bulkEntriesViewModel;
    this.amountChangeListenerFromAlert = getAmountChangedListener();
    this.amountChangeListenerFromTrashcan = amountChangeListenerFromTrashcan;
    this.movementStatusListener = movementStatusListener;
    this.verifyListener = verifyListener;
    super.initLotListView(bulkEntriesViewModel);

    setRvLotInfoReviewWhenItemIsDone();
    setViewStatus();
  }

  @Override
  public void initExistingLotListView() {
    existingLotListView.setLayoutManager(getEnScrollLinearLayoutManager());
    existingLotListView.setHasFixedSize(true);
    BulkEntriesLotMovementAdapter existingBulkEntriesLotMovementAdapter = new BulkEntriesLotMovementAdapter(
        viewModel.getExistingLotMovementViewModelList(), bulkEntriesViewModel, bulkEntriesAdapter);
    existingLotListView.setAdapter(existingBulkEntriesLotMovementAdapter);
    existingBulkEntriesLotMovementAdapter.setAmountChangeListener(amountChangeListenerFromAlert);
    setItemDecoration(existingLotListView);
  }

  @Override
  public void initNewLotListView() {
    newLotListView.setLayoutManager(getEnScrollLinearLayoutManager());
    newLotListView.setHasFixedSize(true);
    BulkEntriesLotMovementAdapter newBulkEntriesLotMovementAdapter = new BulkEntriesLotMovementAdapter(
        viewModel.getNewLotMovementViewModelList(), bulkEntriesViewModel, bulkEntriesAdapter);
    newLotListView.setAdapter(newBulkEntriesLotMovementAdapter);
    setItemDecoration(newLotListView);
  }

  @Override
  public void addNewLot(LotMovementViewModel lotMovementViewModel) {
    lotMovementViewModel.setFrom(BulkEntriesActivity.KEY_FROM_BULK_ENTRIES_COMPLETE);
    bulkEntriesViewModel.setValidationType(ValidationType.VALID);
    bulkEntriesViewModel.getNewLotMovementViewModelList().add(lotMovementViewModel);
    bulkEntriesAdapter.notifyDataSetChanged();
  }

  @Override
  protected void init(Context context) {
    super.init(context);
    btnVerify.setOnClickListener(v -> {
      if (verifyValidation()) {
        markDone(true);
      }
    });
    btnEdit.setOnClickListener(v -> markDone(false));
  }

  @Override
  protected void inflateLayout(Context context) {
    inflate(context, R.layout.view_lot_list_bulk_entries, this);
  }

  @NonNull
  protected BulkEntriesLotMovementViewHolder.AmountChangeListener getAmountChangedListener() {
    return this::updateAddPositiveLotAmountAlert;
  }

  private void setItemDecoration(RecyclerView recyclerView) {
    if (recyclerView.getItemDecorationCount() == 0) {
      recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }
  }

  private LinearLayoutManager getEnScrollLinearLayoutManager() {
    return new LinearLayoutManager(getContext()) {
      @Override
      public boolean canScrollVertically() {
        return false;
      }
    };
  }

  private void setViewStatus() {
    if (bulkEntriesViewModel.getValidationType() == ValidationType.NO_LOT) {
      btnAddNewLot.setBackground(getDrawable(R.drawable.border_round_red));
      btnAddNewLot.setTextColor(getColor(R.color.color_red));
      alertAddPositiveLotAmount.setVisibility(VISIBLE);
    } else if (bulkEntriesViewModel.getValidationType() == ValidationType.EXISTING_LOT_ALL_AMOUNT_BLANK) {
      alertAddPositiveLotAmount.setVisibility(VISIBLE);
      btnAddNewLot.setBackground(getDrawable(R.drawable.border_round_blue));
      btnAddNewLot.setTextColor(getColor(R.color.color_accent));
    } else {
      alertAddPositiveLotAmount.setVisibility(GONE);
      btnAddNewLot.setBackground(getDrawable(R.drawable.border_round_blue));
      btnAddNewLot.setTextColor(getColor(R.color.color_accent));
    }
  }

  private void setRvLotInfoReviewWhenItemIsDone() {
    if (bulkEntriesViewModel.isDone()) {
      rvLots.setVisibility(GONE);
      actionPanel.setVisibility(GONE);
      vgLotInfoReview.setVisibility(VISIBLE);
      initLotInfoReviewList();
    } else {
      rvLots.setVisibility(VISIBLE);
      actionPanel.setVisibility(VISIBLE);
      vgLotInfoReview.setVisibility(GONE);
    }
  }

  private void updateAddPositiveLotAmountAlert() {
    bulkEntriesViewModel.validate();
    if (bulkEntriesViewModel.getValidationType() == ValidationType.EXISTING_LOT_ALL_AMOUNT_BLANK) {
      alertAddPositiveLotAmount.setVisibility(View.VISIBLE);
    } else {
      alertAddPositiveLotAmount.setVisibility(View.GONE);
    }
    amountChangeListenerFromTrashcan.onAmountChange();
  }

  private void markDone(boolean done) {
    ((BulkEntriesViewModel) viewModel).setDone(done);
    rvLots.setVisibility(done ? GONE : VISIBLE);
    actionPanel.setVisibility(done ? GONE : VISIBLE);
    vgLotInfoReview.setVisibility(done ? VISIBLE : GONE);
    movementStatusListener.onMovementStatusChange();
    if (done) {
      initLotInfoReviewList();
    }
  }

  private boolean verifyValidation() {
    boolean flag = bulkEntriesViewModel.validate();
    verifyListener.onVerifyListener();
    return flag;
  }

  private void initLotInfoReviewList() {
    BulkLotInfoReviewListAdapter adapter = new BulkLotInfoReviewListAdapter(viewModel,
        Constants.FROM_BULK_ENTRIES_PAGE);
    rvLotInfoReview.setLayoutManager(new LinearLayoutManager(context));
    rvLotInfoReview.setAdapter(adapter);
  }

  private Drawable getDrawable(int id) {
    return ResourcesCompat.getDrawable(getResources(), id, null);
  }

  private int getColor(int id) {
    return ContextCompat.getColor(LMISApp.getContext(), id);
  }

  public interface MovementStatusListener {

    void onMovementStatusChange();
  }

  public interface VerifyListener {

    void onVerifyListener();
  }
}
