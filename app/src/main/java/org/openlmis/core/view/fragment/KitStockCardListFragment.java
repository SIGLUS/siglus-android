package org.openlmis.core.view.fragment;

import android.os.Bundle;

public class KitStockCardListFragment extends StockCardListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.createKitStockCards();
    }
}
