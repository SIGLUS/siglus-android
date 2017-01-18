package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class StockMovementHistoryViewModel {
    StockCard stockCard;

    private List<StockMovementItem> filteredMovementList = new ArrayList<>();

    public StockMovementHistoryViewModel(StockCard stockCard) {

    }

    public void filter(int days) {

    }

    public List<StockMovementItem> getFilteredMovementItemList() {
        return filteredMovementList;
    }

    public String getProductName() {
        return stockCard.getProduct().getFormattedProductNameWithoutStrengthAndType();
    }


    public String getProductUnit() {
        return stockCard.getProduct().getUnit();
    }
}
