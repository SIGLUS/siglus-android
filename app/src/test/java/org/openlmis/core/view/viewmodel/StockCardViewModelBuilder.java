package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

import java.util.List;

public class StockCardViewModelBuilder {

    private InventoryViewModel viewModel;

    public StockCardViewModelBuilder(Product product) {
        viewModel = new InventoryViewModel(product);
    }

    public StockCardViewModelBuilder(StockCard archivedStockCard) {
        viewModel = new InventoryViewModel(archivedStockCard);
    }

    public StockCardViewModelBuilder setSOH(Long soh){
        viewModel.setStockOnHand(soh);
        return this;
    }

    public StockCardViewModelBuilder setChecked(boolean isChecked) {
        viewModel.setChecked(isChecked);
        return this;
    }

    public StockCardViewModelBuilder setType(String type) {
        viewModel.setType(type);
        return this;
    }


    public StockCardViewModelBuilder setQuantity(String quantity) {
        viewModel.setQuantity(quantity);
        return this;
    }

    public StockCardViewModelBuilder setExpiryDates(List<String> dates) {
        viewModel.setExpiryDates(dates);
        return this;
    }

    public StockCardViewModelBuilder setValid(boolean isValid) {
        viewModel.setValid(isValid);
        return this;
    }

    public StockCardViewModelBuilder setKitExpectQuantity(long kitExpectQuantity) {
        viewModel.setKitExpectQuantity(kitExpectQuantity);
        return this;
    }

    public InventoryViewModel build() {
        return viewModel;
    }
}
