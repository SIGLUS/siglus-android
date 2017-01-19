package org.openlmis.core.view.viewmodel;

import org.openlmis.core.LMISApp;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class StockHistoryViewModel {
    StockCard stockCard;

    List<StockHistoryMovementItemViewModel> filteredMovementItemViewModelList = new ArrayList<>();

    List<StockHistoryMovementItemViewModel> allMovementItemViewModelList = new ArrayList<>();


    public StockHistoryViewModel(StockCard stockCard) {
        this.stockCard = stockCard;
        allMovementItemViewModelList.addAll(FluentIterable.from(stockCard.getStockMovementItemsWrapper()).transform(new Function<StockMovementItem, StockHistoryMovementItemViewModel>() {
            @Override
            public StockHistoryMovementItemViewModel apply(StockMovementItem stockMovementItem) {
                return new StockHistoryMovementItemViewModel(stockMovementItem);
            }
        }).toList());
    }

    public List<StockHistoryMovementItemViewModel> filter(final int days) {
        filteredMovementItemViewModelList.clear();
        filteredMovementItemViewModelList.addAll(FluentIterable.from(allMovementItemViewModelList).filter(new Predicate<StockHistoryMovementItemViewModel>() {
            @Override
            public boolean apply(StockHistoryMovementItemViewModel stockHistoryMovementItemViewModel) {
                return !(stockHistoryMovementItemViewModel.getStockMovementItem().getMovementDate()).before(DateUtil.minusDayOfMonth(new Date(LMISApp.getInstance().getCurrentTimeMillis()), days));
            }
        }).toList());
        return filteredMovementItemViewModelList;
    }

    public String getProductName() {
        return stockCard.getProduct().getFormattedProductNameWithoutStrengthAndType();
    }


    public String getProductUnit() {
        return stockCard.getProduct().getUnit();
    }
}
