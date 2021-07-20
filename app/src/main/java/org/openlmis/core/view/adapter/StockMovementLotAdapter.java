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

import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementHistoryViewModel;

public class StockMovementLotAdapter extends BaseQuickAdapter<LotMovementHistoryViewModel, BaseViewHolder> {

  public StockMovementLotAdapter() {
    super(R.layout.view_stock_movement_line);
  }

  @Override
  protected void convert(@NonNull BaseViewHolder holder, LotMovementHistoryViewModel model) {
    holder.setText(R.id.tv_reason, model.getReason().getDescription());
    holder.setText(R.id.tv_lot_code, model.getLotCode());
    holder.setText(R.id.tv_document_number, model.getDocumentNo());
    holder.setText(R.id.tv_received, model.getReceived());
    holder.setText(R.id.tv_negative_adjustment, model.getNegativeAdjustment());
    holder.setText(R.id.tv_positive_adjustment, model.getPositiveAdjustment());
    holder.setText(R.id.tv_issued, model.getIssued());
    holder.setText(R.id.tv_stock_on_hand, model.getStockOnHand());

    holder.setTextColorRes(R.id.tv_reason, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);
    holder.setTextColorRes(R.id.tv_lot_code, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);
    holder.setTextColorRes(R.id.tv_document_number, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);
    holder.setTextColorRes(R.id.tv_received, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);
    holder
        .setTextColorRes(R.id.tv_negative_adjustment, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);
    holder
        .setTextColorRes(R.id.tv_positive_adjustment, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);
    holder.setTextColorRes(R.id.tv_issued, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);
    holder.setTextColorRes(R.id.tv_stock_on_hand, model.needShowRed() ? R.color.color_de1313 : R.color.color_black);

    holder.setBackgroundResource(R.id.ll_stock_movement_root, R.color.color_white);
  }
}
