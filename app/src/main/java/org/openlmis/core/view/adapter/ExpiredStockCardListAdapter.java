package org.openlmis.core.view.adapter;

import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

public class ExpiredStockCardListAdapter extends StockCardListAdapter {

    public ExpiredStockCardListAdapter(List<InventoryViewModel> inventoryViewModels, StockCardViewHolder.OnItemViewClickListener listener) {
        super(inventoryViewModels, listener);
    }
}
