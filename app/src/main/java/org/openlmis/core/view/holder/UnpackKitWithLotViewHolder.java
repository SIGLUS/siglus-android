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
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.CompatUtil;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.UnpackKitInventoryViewModel;
import org.openlmis.core.view.widget.MovementChangeLotListView;
import roboguice.inject.InjectView;
import rx.functions.Action1;

public class UnpackKitWithLotViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_kit_expected_quantity)
  TextView tvKitExpectedQuantity;

  @InjectView(R.id.tv_alert_quantity_message)
  TextView tvQuantityMessage;

  @InjectView(R.id.tv_confirm_no_stock)
  TextView tvConfirmNoStock;

  @InjectView(R.id.tv_confirm_has_stock)
  TextView tvConfirmHasStock;

  @InjectView(R.id.vg_soh_pop)
  ViewGroup vgSohPop;

  @InjectView(R.id.tv_product_name)
  TextView tvProductName;

  @InjectView(R.id.tv_product_unit)
  TextView tvProductUnit;

  @InjectView(R.id.view_lot_list)
  MovementChangeLotListView lotListView;

  protected InventoryViewModel viewModel;

  public UnpackKitWithLotViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final InventoryViewModel viewModel,
      Action1<UnpackKitInventoryViewModel> setConfirmNoStockReceivedAction) {
    this.viewModel = viewModel;
    lotListView.initLotListView(viewModel, getMovementChangedListener());

    initViewHolderStyle(viewModel);
    setConfirmStockClickListener((UnpackKitInventoryViewModel) viewModel, setConfirmNoStockReceivedAction);

    validateIfShouldShowUpEmptyLotWarning(viewModel);
    updatePop(viewModel);
  }

  private LotMovementAdapter.MovementChangedListener getMovementChangedListener() {
    return () -> updatePop(viewModel);
  }

  private void validateIfShouldShowUpEmptyLotWarning(InventoryViewModel inventoryViewModel) {
    if (((UnpackKitInventoryViewModel) inventoryViewModel).shouldShowEmptyLotWarning()) {
      vgSohPop.setVisibility(View.VISIBLE);
      vgSohPop.setBackgroundResource(R.drawable.inventory_pop);

      tvConfirmHasStock.setVisibility(View.GONE);
      tvConfirmNoStock.setVisibility(View.VISIBLE);

      tvQuantityMessage.setText(
          LMISApp.getContext().getResources().getString(R.string.message_no_stock_amount_change));
      tvQuantityMessage.setTextColor(ContextCompat.getColor(context, R.color.color_black));
      tvKitExpectedQuantity.setTextColor(ContextCompat.getColor(context, R.color.color_red));
    }

    if (((UnpackKitInventoryViewModel) inventoryViewModel).isConfirmedNoStockReceived()) {
      vgSohPop.setVisibility(View.VISIBLE);
      vgSohPop.setBackgroundResource(R.drawable.inventory_pop);

      tvConfirmHasStock.setVisibility(View.VISIBLE);
      tvConfirmNoStock.setVisibility(View.GONE);

      tvQuantityMessage.setText(
          LMISApp.getContext().getResources().getString(R.string.message_no_stock_received));
      tvQuantityMessage.setTextColor(ContextCompat.getColor(context, R.color.color_black));
      tvKitExpectedQuantity.setTextColor(ContextCompat.getColor(context, R.color.color_black));
      lotListView.setVisibility(View.GONE);
    }
  }

  protected void updatePop(InventoryViewModel viewModel) {
    long totalQuantity = viewModel.getLotListQuantityTotalAmount();
    long kitExpectQuantity = viewModel.getKitExpectQuantity();

    if (((UnpackKitInventoryViewModel) viewModel).hasLotChanged()) {
      if (totalQuantity == kitExpectQuantity) {
        initViewHolderStyle(viewModel);
      } else {
        vgSohPop.setVisibility(View.VISIBLE);
        vgSohPop.setBackgroundResource(R.drawable.inventory_pop_warning);
        tvConfirmHasStock.setVisibility(View.GONE);
        tvConfirmNoStock.setVisibility(View.GONE);
        tvKitExpectedQuantity.setTextColor(ContextCompat.getColor(context, R.color.color_black));
        tvQuantityMessage.setTextColor(ContextCompat.getColor(context, R.color.color_warning_text_unpack_kit_pop));
        if (totalQuantity > kitExpectQuantity) {
          tvQuantityMessage.setText(CompatUtil.fromHtml(
              context.getString(R.string.label_unpack_kit_quantity_more_than_expected)));
        } else {
          tvQuantityMessage.setText(CompatUtil.fromHtml(
              context.getString(R.string.label_unpack_kit_quantity_less_than_expected)));
        }
      }
    } else {
      initViewHolderStyle(viewModel);
      validateIfShouldShowUpEmptyLotWarning(viewModel);
    }
  }

  private void initViewHolderStyle(InventoryViewModel inventoryViewModel) {
    lotListView.setVisibility(View.VISIBLE);
    vgSohPop.setVisibility(View.GONE);
    tvKitExpectedQuantity.setTextColor(ContextCompat.getColor(context, R.color.color_black));
    tvProductName.setText(TextStyleUtil
        .getHighlightQueryKeyWord(StringUtils.EMPTY, inventoryViewModel.getStyledName()));
    tvProductUnit.setText(TextStyleUtil
        .getHighlightQueryKeyWord(StringUtils.EMPTY, inventoryViewModel.getStyledUnit()));
    tvKitExpectedQuantity
        .setText(this.context.getResources().getString(R.string.text_quantity_expected,
            Long.toString(inventoryViewModel.getKitExpectQuantity())));
  }

  private void setConfirmStockClickListener(final UnpackKitInventoryViewModel inventoryViewModel,
      final Action1<UnpackKitInventoryViewModel> setConfirmNoStockReceivedAction) {
    tvConfirmNoStock.setOnClickListener(view -> {
      tvConfirmNoStock.setVisibility(View.GONE);
      tvConfirmHasStock.setVisibility(View.VISIBLE);
      tvQuantityMessage.setText(
          LMISApp.getContext().getResources().getString(R.string.message_no_stock_received));
      tvKitExpectedQuantity.setTextColor(ContextCompat.getColor(context, R.color.color_black));
      lotListView.setVisibility(View.GONE);
      setConfirmNoStockReceivedAction.call(inventoryViewModel);
    });
    tvConfirmHasStock.setOnClickListener(view -> {
      tvConfirmHasStock.setVisibility(View.GONE);
      tvConfirmNoStock.setVisibility(View.VISIBLE);
      tvQuantityMessage.setText(
          LMISApp.getContext().getResources().getString(R.string.message_no_stock_amount_change));
      tvKitExpectedQuantity.setTextColor(ContextCompat.getColor(context, R.color.color_red));
      lotListView.setVisibility(View.VISIBLE);
      inventoryViewModel.setConfirmedNoStockReceived(false);
      inventoryViewModel.setShouldShowEmptyLotWarning(true);
    });
  }
}
