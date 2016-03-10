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

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.Constants;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_stock_movement_history)
public class StockMovementHistoryActivity extends BaseActivity {

    private boolean isKit;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.StockCardMovementHistoryScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isKit = getIntent().getBooleanExtra(Constants.PARAM_IS_KIT, false);
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra(Constants.PARAM_STOCK_NAME));

        if (getIntent().getBooleanExtra(Constants.PARAM_IS_FROM_ARCHIVE, false) && getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white);
        }
    }

    @Override
    protected int getThemeRes() {
        return isKit ? R.style.AppTheme_TEAL : super.getThemeRes();
    }

    public static Intent getIntentToMe(Context context, long stockCardId, String stockName,
                                       boolean isFromArchive, boolean isKit) {
        Intent intent = new Intent(context, StockMovementHistoryActivity.class);
        intent.putExtra(Constants.PARAM_STOCK_CARD_ID, stockCardId);
        intent.putExtra(Constants.PARAM_STOCK_NAME, stockName);
        intent.putExtra(Constants.PARAM_IS_FROM_ARCHIVE, isFromArchive);
        intent.putExtra(Constants.PARAM_IS_KIT, isKit);
        return intent;
    }
}
