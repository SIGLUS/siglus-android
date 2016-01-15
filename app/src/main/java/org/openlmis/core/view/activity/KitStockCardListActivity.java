package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.view.Menu;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.fragment.KitStockCardListFragment;
import org.openlmis.core.view.fragment.StockCardListFragment;

public class KitStockCardListActivity extends StockCardListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_home_page_update)) {
            setTheme(R.style.AppTheme_TEAL);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected StockCardListFragment createFragment() {
        return new KitStockCardListFragment();
    }

    @Override
    protected void addMenus(Menu menu) {
        //kit stock overview should not have any menu items(search, add new product, archived drug)
        menu.clear();
    }
}
