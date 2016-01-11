package org.openlmis.core.view.adapter;

import android.view.View;

import org.openlmis.core.view.holder.KitStockCardViewHolder;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.List;

public class KitStockCardListAdapter extends StockCardListAdapter {

    public KitStockCardListAdapter(List<StockCardViewModel> stockCardViewModel, StockCardViewHolder.OnItemViewClickListener listener) {
        super(stockCardViewModel, listener);
    }

    @Override
    protected StockCardViewHolder createViewHolder(View view) {
        return new KitStockCardViewHolder(view, listener);
    }
}
