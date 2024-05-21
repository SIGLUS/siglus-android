package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;

import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.view.fragment.ExpiredStockCardListFragment;
import org.openlmis.core.view.fragment.StockCardListFragment;

public class ExpiredStockCardListActivity extends StockCardListActivity {

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.EXPIRED_PRODUCTS_SCREEN;
    }

    @Override
    protected StockCardListFragment createFragment() {
        return new ExpiredStockCardListFragment();
    }

    @Override
    protected void addMenus(Menu menu) {
        // just keep search option
    }

    public static Intent getIntentToMe(Context context) {
        return new Intent(context, ExpiredStockCardListActivity.class);
    }
}