package org.openlmis.core.view.fragment;

import org.openlmis.core.view.adapter.KitStockCardListAdapter;

public class KitStockCardListFragment extends StockCardListFragment {
    @Override
    protected void loadStockCards() {
        presenter.loadKits();
    }

    @Override
    protected void createAdapter() {
        mAdapter = new KitStockCardListAdapter(stockCardViewModels, onItemViewClickListener);
    }
}
