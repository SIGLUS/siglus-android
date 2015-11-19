package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.Product;

import java.util.List;

public class StockCardViewModelBuilder {

    private StockCardViewModel viewModel;

    public StockCardViewModelBuilder(Product product) {
        viewModel = new StockCardViewModel(product);
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

    public StockCardViewModel build() {
        return viewModel;
    }

    public StockCardViewModelBuilder setValidate(boolean isValidate) {
        viewModel.setValidate(isValidate);
        return this;
    }
}
