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
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.BaseStockMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class BaseLotListView extends FrameLayout {

  public static final String ADD_LOT = "add_new_lot";
  protected Context context;

  @InjectView(R.id.ly_action_panel)
  protected View actionPanel;

  @InjectView(R.id.btn_add_new_lot)
  protected TextView btnAddNewLot;

  @InjectView(R.id.rv_new_lot_list)
  protected RecyclerView newLotListView;

  @InjectView(R.id.rv_existing_lot_list)
  protected RecyclerView existingLotListView;

  protected AddLotDialogFragment addLotDialogFragment;

  protected LotMovementAdapter newLotMovementAdapter;
  protected LotMovementAdapter existingLotMovementAdapter;

  @Setter
  protected BaseStockMovementViewModel viewModel;

  public BaseLotListView(Context context) {
    super(context);
  }

  public BaseLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public void initLotListView(BaseStockMovementViewModel viewModel) {
    this.viewModel = viewModel;
    initExistingLotListView();
    initNewLotListView();
    setLotListVisible();
    btnAddNewLot.setOnClickListener(getAddNewLotOnClickListener());
  }

  public void initExistingLotListView() {
    existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    existingLotMovementAdapter = new LotMovementAdapter(viewModel.getExistingLotMovementViewModelList());
    existingLotListView.setAdapter(existingLotMovementAdapter);
  }

  public void initNewLotListView() {
    newLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    newLotMovementAdapter = new LotMovementAdapter(viewModel.getNewLotMovementViewModelList(),
        viewModel.getProduct().getProductNameWithCodeAndStrength());
    newLotListView.setAdapter(newLotMovementAdapter);
  }

  public void setActionAddNewEnabled(boolean actionAddNewEnabled) {
    btnAddNewLot.setEnabled(actionAddNewEnabled);
  }

  public void refreshNewLotList() {
    setLotListVisible();
    newLotMovementAdapter.notifyDataSetChanged();
  }

  public void addNewLot(LotMovementViewModel lotMovementViewModel) {
    viewModel.getNewLotMovementViewModelList().add(lotMovementViewModel);
    refreshNewLotList();
  }

  public AddLotDialogFragment.AddLotWithoutNumberListener getAddLotWithoutNumberListener() {
    return expiryDate -> {
      btnAddNewLot.setEnabled(true);
      String lotNumber = LotMovementViewModel
          .generateLotNumberForProductWithoutLot(viewModel.getProduct().getCode(), expiryDate);
      if (getLotNumbers().contains(lotNumber)) {
        ToastUtil.show(LMISApp.getContext().getString(R.string.error_lot_without_number_already_exists));
      } else {
        addNewLot(new LotMovementViewModel(lotNumber, expiryDate,
            MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
      }
    };
  }

  @NonNull
  public List<String> getLotNumbers() {
    final List<String> existingLots = new ArrayList<>();
    existingLots.addAll(FluentIterable.from(viewModel.getNewLotMovementViewModelList())
        .transform(LotMovementViewModel::getLotNumber).toList());
    existingLots.addAll(FluentIterable.from(viewModel.getExistingLotMovementViewModelList())
        .transform(LotMovementViewModel::getLotNumber).toList());
    return existingLots;
  }

  @NonNull
  public SingleClickButtonListener getAddNewLotOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        showAddLotDialogFragment();
      }
    };
  }

  public boolean showAddLotDialogFragment() {
    Bundle bundle = new Bundle();
    bundle.putString(Constants.PARAM_STOCK_NAME, viewModel.getProduct().getFormattedProductName());
    addLotDialogFragment = new AddLotDialogFragment();
    addLotDialogFragment.setArguments(bundle);
    addLotDialogFragment.setListener(getAddNewLotDialogOnClickListener());
    addLotDialogFragment.setOnDismissListener(getOnAddNewLotDialogDismissListener());
    addLotDialogFragment.setAddLotWithoutNumberListener(getAddLotWithoutNumberListener());
    addLotDialogFragment.show(((BaseActivity) context).getSupportFragmentManager(), ADD_LOT);
    return true;
  }

  @NonNull
  public OnDismissListener getOnAddNewLotDialogDismissListener() {
    return () -> setActionAddNewEnabled(true);
  }

  protected void init(Context context) {
    this.context = context;
    inflateLayout(context);
    RoboGuice.injectMembers(getContext(), this);
    RoboGuice.getInjector(getContext()).injectViewMembers(this);
  }

  protected void inflateLayout(Context context) {
    inflate(context, R.layout.view_lot_list, this);
  }

  @NonNull
  protected SingleClickButtonListener getAddNewLotDialogOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        switch (v.getId()) {
          case R.id.btn_complete:
            if (addLotDialogFragment.validate() && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
              addNewLot(new LotMovementViewModel(addLotDialogFragment.getLotNumber(),
                  addLotDialogFragment.getExpiryDate(), viewModel.getMovementType()));
              addLotDialogFragment.dismiss();
            }
            break;
          case R.id.btn_cancel:
            addLotDialogFragment.dismiss();
            break;
          default:
            // do nothing
        }
      }
    };
  }

  private void setLotListVisible() {
    if (existingLotListView != null) {
      boolean existingLotsVisible = !CollectionUtils.isEmpty(viewModel.getExistingLotMovementViewModelList());
      existingLotListView.setVisibility(existingLotsVisible ? VISIBLE : GONE);
    }
    if (newLotListView != null) {
      boolean newLotsVisible = !CollectionUtils.isEmpty(viewModel.getNewLotMovementViewModelList());
      newLotListView.setVisibility(newLotsVisible ? VISIBLE : GONE);
    }
  }

  public interface OnDismissListener {

    void onDismissAction();
  }
}
