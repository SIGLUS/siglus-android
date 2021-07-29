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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.adapter.NewMovementLotMovementAdapter;
import org.openlmis.core.view.viewmodel.BaseStockMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import roboguice.inject.InjectView;

public class NewMovementLotListView extends MovementChangeLotListView {

  @InjectView(R.id.alert_add_positive_lot_amount)
  ViewGroup alertAddPositiveLotAmount;

  @InjectView(R.id.alert_soonest_expire)
  ViewGroup alertSoonestExpire;

  @InjectView(R.id.alert_soonest_and_contain_expire)
  TextView textViewAlertSoonestAndExpired;

  public NewMovementLotListView(Context context) {
    super(context);
  }

  public NewMovementLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void initLotListView(BaseStockMovementViewModel viewModel) {
    this.viewModel = viewModel;
    movementChangedListener = getMovementChangedListener();

    if (MovementReasonManager.MovementType.RECEIVE.equals(viewModel.getMovementType())
        || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(viewModel.getMovementType())) {
      setActionAddNewLotVisibility(View.VISIBLE);
      setActionAddNewLotListener(getAddNewLotOnClickListener());
    } else {
      setActionAddNewLotVisibility(GONE);
    }
    initExistingLotListView();
    initNewLotListView();
    initLotErrorBanner();
  }

  @Override
  public void addNewLot(LotMovementViewModel lotMovementViewModel) {
    super.addNewLot(lotMovementViewModel);
    updateAddPositiveLotAmountAlert();
  }

  @NonNull
  protected LotMovementAdapter.MovementChangedListener getMovementChangedListener() {
    return () -> {
      updateAddPositiveLotAmountAlert();
      updateSoonestToExpireNotIssuedBanner();
    };
  }

  @Override
  public void initExistingLotListView() {
    existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    existingLotMovementAdapter = new NewMovementLotMovementAdapter(
        viewModel.getExistingLotMovementViewModelList());
    existingLotListView.setAdapter(existingLotMovementAdapter);
    existingLotMovementAdapter.setMovementChangeListener(movementChangedListener);
  }

  @Override
  public void initNewLotListView() {
    newLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    newLotMovementAdapter = new NewMovementLotMovementAdapter(
        viewModel.getNewLotMovementViewModelList(),
        viewModel.getProduct().getProductNameWithCodeAndStrength());
    newLotListView.setAdapter(newLotMovementAdapter);
    newLotMovementAdapter.setMovementChangeListener(movementChangedListener);
  }

  private void updateAddPositiveLotAmountAlert() {
    if (!((StockMovementViewModel) viewModel).movementQuantitiesExist()) {
      alertAddPositiveLotAmount.setVisibility(View.VISIBLE);
    } else {
      alertAddPositiveLotAmount.setVisibility(View.GONE);
    }
  }

  private void updateSoonestToExpireNotIssuedBanner() {
    if (MovementReasonManager.MovementType.ISSUE == viewModel.getMovementType()) {
      LotStatus lotStatus = ((StockMovementViewModel) viewModel).getSoonestToExpireLotsIssued();
      if (lotStatus == LotStatus.DEFAULT_STATUS) {
        alertSoonestExpire.setVisibility(View.GONE);
      } else if (lotStatus == LotStatus.CONTAIN_EXPIRED_LOTS) {
        alertSoonestExpire.setVisibility(View.VISIBLE);
        textViewAlertSoonestAndExpired.setText(R.string.alert_issue_with_expired);
      } else if (lotStatus == LotStatus.NOT_SOONEST_TO_EXPIRE_LOTS_ISSUED) {
        alertSoonestExpire.setVisibility(View.VISIBLE);
        textViewAlertSoonestAndExpired.setText(R.string.alert_soonest_expire);
      }
    } else {
      alertSoonestExpire.setVisibility(View.GONE);
    }
  }

  public void setActionAddNewLotVisibility(int visibility) {
    actionPanel.setVisibility(visibility);
  }

  public void setActionAddNewLotListener(OnClickListener addNewLotOnClickListener) {
    btnAddNewLot.setOnClickListener(addNewLotOnClickListener);
  }

  public void initLotErrorBanner() {
    if (((StockMovementViewModel) viewModel).hasLotDataChanged()) {
      updateAddPositiveLotAmountAlert();
    }
  }

  public void refresh() {
    existingLotMovementAdapter.notifyDataSetChanged();
    newLotMovementAdapter.notifyDataSetChanged();
  }

  public void setAlertAddPositiveLotAmountVisibility(int visibility) {
    alertAddPositiveLotAmount.setVisibility(visibility);
  }

  public boolean validateLotListWithValidValues() {
    int position1 = ((NewMovementLotMovementAdapter) existingLotMovementAdapter)
        .validateLotQuantityNotGreaterThanSOH();
    if (position1 >= 0) {
      existingLotListView.scrollToPosition(position1);
      existingLotListView.post(existingLotListView::requestFocus);
      return false;
    }
    int position2 = ((NewMovementLotMovementAdapter) newLotMovementAdapter)
        .validateLotPositiveQuantity();
    if (position2 >= 0) {
      newLotListView.scrollToPosition(position2);
      newLotListView.post(newLotListView::requestFocus);
      return false;
    }
    return true;
  }

  public boolean validate() {
    if (validateLotListNotEmpty() && validateLotListWithValidValues()) {
      return true;
    }
    refresh();
    return false;
  }

  public boolean validateLotListNotEmpty() {
    if (((StockMovementViewModel) viewModel).isLotEmpty()) {
      ToastUtil.show(getResources().getString(R.string.empty_lot_warning));
      return false;
    }
    if (!((StockMovementViewModel) viewModel).movementQuantitiesExist()) {
      setAlertAddPositiveLotAmountVisibility(View.VISIBLE);
      return false;
    }
    return true;
  }

  public enum LotStatus {
    NOT_SOONEST_TO_EXPIRE_LOTS_ISSUED,
    CONTAIN_EXPIRED_LOTS,
    DEFAULT_STATUS,
  }
}
