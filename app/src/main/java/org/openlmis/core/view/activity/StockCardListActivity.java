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
import android.view.MenuItem;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.fragment.StockCardListFragment;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_stockcard_list)
public class StockCardListActivity extends SearchBarActivity {

    protected static final int MENU_ID_ADD_NEW_DRUG = 100;
    protected static final int MENU_ID_ARCHIVE_LIST = 200;
    protected StockCardListFragment stockCardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stockCardFragment = (StockCardListFragment) getFragmentManager().findFragmentById(R.id.stock_card_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_ID_ADD_NEW_DRUG, 100, getString(R.string.action_add_new_drug)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ID_ARCHIVE_LIST, 200, getString(R.string.action_navigate_archive)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_ID_ADD_NEW_DRUG:
                startActivity(InventoryActivity.getIntentToMe(this, true));
                return true;
            case MENU_ID_ARCHIVE_LIST:
                startActivity(ArchivedDrugsListActivity.getIntentToMe(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSearchStart(String query) {
        stockCardFragment.onSearch(query);
        return true;
    }

    @Override
    public boolean onSearchClosed() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.search_view_enhancement)){
            return true;
        }
        return false;
    }

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, StockCardListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
}
