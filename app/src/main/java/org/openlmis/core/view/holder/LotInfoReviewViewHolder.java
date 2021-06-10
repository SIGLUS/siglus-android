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

import android.text.Html;
import android.view.View;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import roboguice.inject.InjectView;

public class LotInfoReviewViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_lot_info_review)
  TextView tvLotInfoReview;

  public LotInfoReviewViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(LotMovementViewModel viewModel) {
    long adjustmentQuantity = viewModel.getAdjustmentQuantity();
    if (adjustmentQuantity == 0) {
      tvLotInfoReview.setText(Html.fromHtml(context
          .getString(R.string.msg_physical_inventory_lot_review_no_adjustment,
              viewModel.getLotNumber())));
    } else if (adjustmentQuantity > 0) {
      tvLotInfoReview.setText(Html.fromHtml(context
          .getString(R.string.msg_physical_inventory_lot_review_positive_adjustment,
              viewModel.getLotNumber(), adjustmentQuantity)));
    } else {
      tvLotInfoReview.setText(Html.fromHtml(context
          .getString(R.string.msg_physical_inventory_lot_review_negative_adjustment,
              viewModel.getLotNumber(), Math.abs(adjustmentQuantity))));
    }
  }
}
