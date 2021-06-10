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

package org.openlmis.core.view.viewmodel;

import android.text.SpannableStringBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.openlmis.core.LMISApp;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.TextStyleUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
public class StockHistoryViewModel {

  StockCard stockCard;

  List<StockHistoryMovementItemViewModel> filteredMovementItemViewModelList = new ArrayList<>();

  List<StockHistoryMovementItemViewModel> allMovementItemViewModelList = new ArrayList<>();
  private SpannableStringBuilder styledProductName;
  private SpannableStringBuilder styledProductUnit;


  public StockHistoryViewModel(StockCard stockCard) {
    this.stockCard = stockCard;
    allMovementItemViewModelList
        .addAll(FluentIterable.from(stockCard.getStockMovementItemsWrapper())
            .transform(
                stockMovementItem -> new StockHistoryMovementItemViewModel(stockMovementItem))
            .toSortedList((lhs, rhs) -> {
              int compareResult = lhs.getStockMovementItem().getMovementDate()
                  .compareTo(rhs.getStockMovementItem().getMovementDate());
              if (compareResult == 0) {
                return lhs.getStockMovementItem().getId() < rhs.getStockMovementItem().getId() ? -1
                    : 1;
              }
              return compareResult;
            }).asList());
  }

  public List<StockHistoryMovementItemViewModel> filter(final int days) {
    Date now = new Date(LMISApp.getInstance().getCurrentTimeMillis());
    filteredMovementItemViewModelList.clear();
    filteredMovementItemViewModelList.addAll(FluentIterable.from(allMovementItemViewModelList)
        .filter(stockHistoryMovementItemViewModel ->
            !(stockHistoryMovementItemViewModel.getStockMovementItem().getMovementDate())
                .before(DateUtil.minusDayOfMonth(now, days))).toList());
    return filteredMovementItemViewModelList;
  }

  public SpannableStringBuilder getStyledProductName() {
    if (styledProductName == null) {
      styledProductName = TextStyleUtil.formatStyledProductName(stockCard.getProduct());
    }
    return styledProductName;
  }


  public SpannableStringBuilder getStyledProductUnit() {
    if (styledProductUnit == null) {
      styledProductUnit = TextStyleUtil.formatStyledProductUnit(stockCard.getProduct());
    }
    return styledProductUnit;
  }
}
