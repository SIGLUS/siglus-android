/*
 *
 *  * This program is part of the OpenLMIS logistics management information
 *  * system platform software.
 *  *
 *  * Copyright © 2015 ThoughtWorks, Inc.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published
 *  * by the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version. This program is distributed in the
 *  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Affero General Public License for more details. You should
 *  * have received a copy of the GNU Affero General Public License along with
 *  * this program. If not, see http://www.gnu.org/licenses. For additional
 *  * information contact info@OpenLMIS.org
 *
 */

package org.openlmis.core.enums;

import androidx.annotation.ColorRes;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;

public enum StockOnHandStatus {
  REGULAR_STOCK("regularStock", R.string.Regular_stock, R.color.color_regular_stock,
      R.color.color_stock_status),
  LOW_STOCK("lowStock", R.string.Low_stock, R.color.color_low_stock, R.color.color_stock_status),
  STOCK_OUT("stockOut", R.string.Stock_out, R.color.color_stock_out, R.color.color_stock_status),
  OVER_STOCK("overStock", R.string.Overstock, R.color.color_over_stock, R.color.color_stock_status);

  private final String messageKey;
  private final int description;
  @ColorRes
  private final int bgColor;
  @ColorRes
  private final int color;

  StockOnHandStatus(String key, int desc, @ColorRes int bgColor, @ColorRes int color) {
    this.messageKey = key;
    this.description = desc;
    this.bgColor = bgColor;
    this.color = color;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public int getDescription() {
    return description;
  }

  public @ColorRes
  int getColor() {
    return color;
  }

  public @ColorRes
  int getBgColor() {
    return bgColor;
  }

  public static StockOnHandStatus calculateStockOnHandLevel(StockCard stockCard) {
    if (stockCard.getStockOnHand() == 0) {
      return StockOnHandStatus.STOCK_OUT;
    }
    if (stockCard.getCMM() < 0) {
      return StockOnHandStatus.REGULAR_STOCK;
    }
    if (stockCard.isLowStock()) {
      return StockOnHandStatus.LOW_STOCK;
    } else if (stockCard.isOverStock()) {
      return StockOnHandStatus.OVER_STOCK;
    }
    return StockOnHandStatus.REGULAR_STOCK;
  }
}
