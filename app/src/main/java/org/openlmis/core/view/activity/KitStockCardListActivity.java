package org.openlmis.core.view.activity;

import android.view.Menu;

import org.openlmis.core.R;
import org.openlmis.core.view.fragment.KitStockCardListFragment;
import org.openlmis.core.view.fragment.StockCardListFragment;

public class KitStockCardListActivity extends StockCardListActivity {

    @Override
    protected StockCardListFragment createFragment() {
        return new KitStockCardListFragment();
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_TEAL;
    }

    @Override
    protected void addMenus(Menu menu) {
        //kit stock overview should not have any menu items(search, add new product, archived drug)
        menu.clear();
    }
}
