package org.openlmis.core.view.activity;

import android.content.Intent;
import android.os.Bundle;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.Constants;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_stock_card_new_movement)
public class StockCardNewMovementActivity extends BaseActivity {

    private String stockName;

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);

        super.onCreate(savedInstanceState);

        initUI();
    }

    private void initUI() {
        setTitle(stockName);
    }

    public static Intent getIntentToMe(StockMovementsActivityNew context, String stockName) {
        Intent intent = new Intent(context, StockCardNewMovementActivity.class);
        intent.putExtra(Constants.PARAM_STOCK_NAME, stockName);
        return intent;
    }

}
