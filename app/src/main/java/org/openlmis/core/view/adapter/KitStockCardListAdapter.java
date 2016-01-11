package org.openlmis.core.view.adapter;

import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.List;

public class KitStockCardListAdapter extends StockCardListAdapter {

    public KitStockCardListAdapter(List<StockCardViewModel> stockCardViewModel, StockCardViewHolder.OnItemViewClickListener listener) {
        super(stockCardViewModel, listener);
    }

}
