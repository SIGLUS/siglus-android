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
import org.openlmis.core.utils.CompatUtil;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import roboguice.inject.InjectView;

public class BulkLotInfoReviewViewHolder extends BaseViewHolder {

  private final String fromWhichPage;

  public BulkLotInfoReviewViewHolder(View itemView, String fromWhichPage) {
    super(itemView);
    this.fromWhichPage = fromWhichPage;
  }

  @InjectView(R.id.tv_lot_info_review)
  TextView tvLotInfoReview;

  public void populate(LotMovementViewModel viewModel) {
    long adjustmentQuantity = Long.parseLong(viewModel.getQuantity());
    if (fromWhichPage.equals(Constants.FROM_BULK_ENTRIES_PAGE)) {
      tvLotInfoReview.setText(CompatUtil.fromHtml(context
          .getString(R.string.msg_bulk_entries_lot_review, viewModel.getLotNumber(),
              adjustmentQuantity)));
    }
    if (fromWhichPage.equals(Constants.FROM_BULK_INITIAL_PAGE)) {
      tvLotInfoReview.setText(CompatUtil.fromHtml(context
          .getString(R.string.msg_initial_inventory_lot_review_add, viewModel.getLotNumber(),
              adjustmentQuantity)));
    }
  }
}
