package org.openlmis.core.view.activity;

import android.view.Menu;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.view.fragment.KitStockCardListFragment;
import org.openlmis.core.view.fragment.StockCardListFragment;

public class KitStockCardListActivity extends StockCardListActivity {

  @Override
  protected StockCardListFragment createFragment() {
    return new KitStockCardListFragment();
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.KIT_STOCK_CARD_OVERVIEW_SCREEN;
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
