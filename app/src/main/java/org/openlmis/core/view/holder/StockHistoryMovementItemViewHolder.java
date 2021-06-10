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
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.StockHistoryMovementItemViewModel;
import roboguice.inject.InjectView;

public class StockHistoryMovementItemViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_movement_date)
  TextView tvMovementDate;

  @InjectView(R.id.tv_movement_reason)
  TextView tvMovementReason;

  @InjectView(R.id.tv_document_number)
  TextView tvDocumentNumber;

  @InjectView(R.id.tv_entry_amount)
  TextView tvEntryAmount;

  @InjectView(R.id.tv_negative_adjustment_amount)
  TextView tvNegativeAdjustmentAmount;

  @InjectView(R.id.tv_positive_adjustment_amount)
  TextView tvPositiveAdjustmentAmount;

  @InjectView(R.id.tv_issue_amount)
  TextView tvIssueAmount;

  @InjectView(R.id.tv_stock_existence)
  TextView tvStockExistence;

  private final int blackColor;
  private final int redColor;

  public StockHistoryMovementItemViewHolder(View itemView) {
    super(itemView);

    blackColor = context.getResources().getColor(R.color.color_black);
    redColor = context.getResources().getColor(R.color.color_red);
  }

  public void populate(StockHistoryMovementItemViewModel viewModel) {
    tvMovementDate.setText(viewModel.getMovementDate());
    tvMovementReason.setText(viewModel.getMovementReason());
    tvDocumentNumber.setText(viewModel.getDocumentNumber());
    tvEntryAmount.setText(viewModel.getEntryAmount());
    tvNegativeAdjustmentAmount.setText(viewModel.getNegativeAdjustmentAmount());
    tvPositiveAdjustmentAmount.setText(viewModel.getPositiveAdjustmentAmount());
    tvIssueAmount.setText(viewModel.getIssueAmount());
    tvStockExistence.setText(viewModel.getStockExistence());

    setTextColor(viewModel.isIssueAdjustment() ? blackColor : redColor);
  }

  private void setTextColor(int color) {
    tvMovementDate.setTextColor(color);
    tvMovementReason.setTextColor(color);
    tvDocumentNumber.setTextColor(color);
    tvEntryAmount.setTextColor(color);
    tvNegativeAdjustmentAmount.setTextColor(color);
    tvPositiveAdjustmentAmount.setTextColor(color);
    tvIssueAmount.setTextColor(color);
    tvStockExistence.setTextColor(color);
  }
}
