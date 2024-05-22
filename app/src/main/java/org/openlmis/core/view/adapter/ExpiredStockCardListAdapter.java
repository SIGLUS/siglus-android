package org.openlmis.core.view.adapter;

import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.ExpiredStockCardListViewHolder;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

public class ExpiredStockCardListAdapter extends StockCardListAdapter {

    public ExpiredStockCardListAdapter(List<InventoryViewModel> inventoryViewModels) {
        super(inventoryViewModels, null);
    }

    @Override
    protected int getItemStockCardLayoutId() {
        return R.layout.item_expired_stock_card;
    }

    @Override
    protected StockCardViewHolder createViewHolder(View view) {
        return new ExpiredStockCardListViewHolder(view);
    }
}
