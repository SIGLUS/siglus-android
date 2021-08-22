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

import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import java.util.Objects;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.view.viewmodel.StockMovementHistoryViewModel;

public class StockMovementAdapter extends BaseQuickAdapter<StockMovementHistoryViewModel, BaseViewHolder> {

  @Setter
  private ScreenName previousPage;

  private BaseViewHolder holder;

  private StockMovementHistoryViewModel model;

  public StockMovementAdapter() {
    super(R.layout.item_stock_movement);
  }

  @Override
  protected void convert(@NonNull BaseViewHolder holder, StockMovementHistoryViewModel model) {
    this.holder = holder;
    this.model = model;
    if (previousPage == ScreenName.STOCK_CARD_MOVEMENT_SCREEN) {
      covertByProduct();
    }
    if (previousPage == ScreenName.STOCK_MOVEMENT_DETAIL_HISTORY_SCREEN) {
      covertByLot();
    }
  }

  private void covertByProduct() {
    setTextAndTextColor(R.id.tv_date, model.getMovementDate());
    setTextAndTextColor(R.id.tv_reason, model.isNoStock()
        ? holder.itemView.getContext().getString(R.string.label_inventory) : model.getMovementDesc());
    setTextAndTextColor(R.id.tv_document_number, model.getDocumentNumber());
    setTextAndTextColor(R.id.tv_received, model.getReceived());
    setTextAndTextColor(R.id.tv_negative_adjustment, model.getNegativeAdjustment());
    setTextAndTextColor(R.id.tv_positive_adjustment, model.getPositiveAdjustment());
    setTextAndTextColor(R.id.tv_issued, model.getIssued());
    setTextAndTextColor(R.id.tv_stock_on_hand, model.getStockOnHand());
    setTextAndTextColor(R.id.tv_requested, model.getRequested());
    setTextAndTextColor(R.id.tv_signature, model.getSignature());
    holder.getView(R.id.tv_lot_code).setVisibility(View.GONE);
    holder.getView(R.id.rv_stock_movement_lot_list).setVisibility(View.GONE);
  }

  private void covertByLot() {
    setTextAndTextColor(R.id.tv_date, model.getMovementDate());
    setTextAndTextColor(R.id.tv_stock_on_hand, model.getStockOnHand());
    setTextAndTextColor(R.id.tv_requested, model.getRequested());
    setTextAndTextColor(R.id.tv_signature, model.getSignature());
    setTextAndTextColor(R.id.tv_reason, model.isNoStock()
        ? holder.itemView.getContext().getString(R.string.label_inventory) : "");
    holder.setBackgroundResource(R.id.ll_stock_movement_root, R.color.color_eeeeee);
    RecyclerView rvLotList = holder.getView(R.id.rv_stock_movement_lot_list);
    if (model.getLotViewModelList().isEmpty()) {
      rvLotList.setVisibility(View.GONE);
    } else {
      rvLotList.setVisibility(View.VISIBLE);
      holder.setText(R.id.tv_reason, "");
      rvLotList.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
      DividerItemDecoration decor = new DividerItemDecoration(holder.itemView.getContext(), LinearLayout.VERTICAL);
      decor.setDrawable(Objects.requireNonNull(ContextCompat
          .getDrawable(holder.itemView.getContext(), R.drawable.shape_stock_movement_history_item_decoration)));
      if (rvLotList.getItemDecorationCount() == 0) {
        rvLotList.addItemDecoration(decor);
      }
      StockMovementLotAdapter lotAdapter = new StockMovementLotAdapter();
      rvLotList.setAdapter(lotAdapter);
      lotAdapter.setList(model.getLotViewModelList());
    }
  }

  private void setTextAndTextColor(int viewId, String text) {
    holder.setText(viewId, text);
    holder.setTextColorRes(viewId, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);
  }
}
