/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_stock_movement_history)
public class StockMovementHistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra(Constants.PARAM_STOCK_NAME));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public static Intent getIntentToMe(Context context, long stockCardId, String stockName) {
        Intent intent = new Intent(context, StockMovementHistoryActivity.class);
        intent.putExtra(Constants.PARAM_STOCK_CARD_ID, stockCardId);
        intent.putExtra(Constants.PARAM_STOCK_NAME, stockName);
        return intent;
    }
}
