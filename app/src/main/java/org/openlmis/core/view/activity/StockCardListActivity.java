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
import org.openlmis.core.view.fragment.StockCardListFragment;

import roboguice.inject.ContentView;
import roboguice.inject.InjectFragment;

@ContentView(R.layout.activity_stockcard_list)
public class StockCardListActivity extends BaseActivity {

    @InjectFragment(R.id.stock_card_list)
    StockCardListFragment stockCardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onSearchStart(String query) {
        stockCardFragment.onSearch(query);
        return true;
    }

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, StockCardListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
}
