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

import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import roboguice.inject.InjectView;

public class KitStockCardViewHolder extends StockCardViewHolder {

  @InjectView(R.id.tv_product_name)
  TextView kitTvProductName;

  @InjectView(R.id.tv_stock_on_hand)
  TextView kitTvStockOnHand;

  //above field are present in base class, but injection does not penetrate sub class
  public KitStockCardViewHolder(View itemView, OnItemViewClickListener listener) {
    super(itemView, listener);
  }

  @Override
  protected void inflateData(InventoryViewModel inventoryViewModel, String queryKeyWord) {
    kitTvStockOnHand.setText(String.valueOf(inventoryViewModel.getStockOnHand()));
    kitTvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord,
        new SpannableStringBuilder(inventoryViewModel.getProduct().getPrimaryName())));
  }
}
