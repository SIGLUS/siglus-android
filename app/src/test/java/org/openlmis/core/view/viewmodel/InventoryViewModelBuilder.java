package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

public class InventoryViewModelBuilder {

    private InventoryViewModel viewModel;

    public InventoryViewModelBuilder(Product product) {
        viewModel = new InventoryViewModel(product);
    }

    public InventoryViewModelBuilder(StockCard archivedStockCard) {
        viewModel = new InventoryViewModel(archivedStockCard);
    }

    public InventoryViewModelBuilder setSOH(Long soh){
        viewModel.setStockOnHand(soh);
        return this;
    }

    public InventoryViewModelBuilder setChecked(boolean isChecked) {
        viewModel.setChecked(isChecked);
        return this;
    }

    public InventoryViewModelBuilder setType(String type) {
        viewModel.setType(type);
        return this;
    }

    public InventoryViewModelBuilder setValid(boolean isValid) {
        viewModel.setValid(isValid);
        return this;
    }

    public InventoryViewModelBuilder setKitExpectQuantity(long kitExpectQuantity) {
        viewModel.setKitExpectQuantity(kitExpectQuantity);
        return this;
    }

    public InventoryViewModel build() {
        return viewModel;
    }
}
