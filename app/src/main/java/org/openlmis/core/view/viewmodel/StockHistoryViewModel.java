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
