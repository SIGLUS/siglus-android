package org.openlmis.core.view.activity;

import org.openlmis.core.view.fragment.KitStockCardListFragment;
import org.openlmis.core.view.fragment.StockCardListFragment;

public class KitStockCardListActivity extends StockCardListActivity {

    @Override
    protected StockCardListFragment createFragment() {
        return new KitStockCardListFragment();
    }
}
