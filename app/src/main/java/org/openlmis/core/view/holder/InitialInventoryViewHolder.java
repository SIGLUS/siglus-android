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

package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.InitialInventoryLotListView;
import roboguice.inject.InjectView;


public class InitialInventoryViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_product_name)
  TextView productName;

  @InjectView(R.id.tv_product_unit)
  TextView productUnit;

  @InjectView(R.id.checkbox)
  CheckBox checkBox;

  @InjectView(R.id.action_view_history)
  TextView tvHistoryAction;

  @InjectView(R.id.touchArea_checkbox)
  LinearLayout taCheckbox;

  @InjectView(R.id.view_lot_list)
  InitialInventoryLotListView lotListView;

  private InventoryViewModel viewModel;

  public InitialInventoryViewHolder(View itemView) {
    super(itemView);
    initView();
  }

  private void initView() {
    taCheckbox.setOnClickListener((v) -> checkBox.setChecked(!checkBox.isChecked()));
  }

  public void populate(final InventoryViewModel inventoryViewModel, String queryKeyWord,
      ViewHistoryListener listener) {
    this.viewModel = inventoryViewModel;
    setUpLotListView();
    resetCheckBox();
    setUpCheckBox();

    checkBox.setChecked(viewModel.isChecked());

    productName
        .setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledName()));
    productUnit
        .setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyleType()));

    initHistoryView(listener);
  }

  public void setUpLotListView() {
    lotListView.setUpdateCheckBoxListener(() -> {
      checkBox.setEnabled(true);
      if (viewModel.getNewLotMovementViewModelList().isEmpty()) {
        checkBox.setChecked(false);
      }
    });
    lotListView.initLotListView(viewModel);
  }

  private void resetCheckBox() {
    checkBox.setEnabled(true);
    checkBox.setOnCheckedChangeListener(null);
    checkBox.setChecked(false);
    lotListView.setVisibility(View.GONE);
  }

  protected void setUpCheckBox() {
    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
      checkBox.setEnabled(false);
      checkedChangeAction(isChecked);
    });
  }

  private void checkedChangeAction(boolean isChecked) {
    if (isChecked && !viewModel.getProduct().isArchived()) {
      if (!viewModel.getNewLotMovementViewModelList().isEmpty()) {
        showAddNewLotPanel(View.VISIBLE);
        checkBox.setEnabled(true);
        viewModel.setChecked(true);
      } else if (lotListView.showAddLotDialogFragment()) {
        showAddNewLotPanel(View.VISIBLE);
        viewModel.setChecked(true);
      } else {
        checkBox.setEnabled(true);
        checkBox.setChecked(false);
      }
    } else {
      checkBox.setEnabled(true);
      showAddNewLotPanel(View.GONE);
      viewModel.getNewLotMovementViewModelList().clear();
      lotListView.refreshNewLotList();
      viewModel.setChecked(isChecked);
    }
  }

  private void initHistoryView(final ViewHistoryListener listener) {
    tvHistoryAction.setVisibility(viewModel.getProduct().isArchived() ? View.VISIBLE : View.GONE);
    tvHistoryAction.setOnClickListener(v -> {
      if (listener != null) {
        listener.viewHistory(viewModel.getStockCard());
      }
    });
  }

  public void showAddNewLotPanel(int visible) {
    lotListView.setVisibility(visible);
  }

  public interface ViewHistoryListener {

    void viewHistory(StockCard stockCard);
  }
}
