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
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.fragment.StockCardListFragment;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_stockcard_list)
public class StockCardListActivity extends SearchBarActivity {

  protected static final int MENU_ID_ADD_NEW_DRUG = 100;
  protected static final int MENU_ID_ARCHIVE_LIST = 200;
  private static final int MENU_ID_MOVEMENT_HISTORY = 300;
  protected StockCardListFragment stockCardFragment;

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.STOCK_CARD_OVERVIEW_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      stockCardFragment = createFragment();
      getFragmentManager().beginTransaction().replace(R.id.stock_card_container, stockCardFragment)
          .commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    addMenus(menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ID_ADD_NEW_DRUG:
        startActivity(InitialInventoryActivity.getIntentToMe(this, true));
        return true;
      case MENU_ID_ARCHIVE_LIST:
        startActivityForResult(ArchivedDrugsListActivity.getIntentToMe(this),
            Constants.REQUEST_ARCHIVED_LIST_PAGE);
        return true;
      case MENU_ID_MOVEMENT_HISTORY:
        startActivity(AllDrugsMovementHistoryActivity.getIntentToMe(this));
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean onSearchStart(String query) {
    if (stockCardFragment != null) {
      stockCardFragment.onSearch(query);
      return true;
    } else {
      return false;
    }
  }

  public static Intent getIntentToMe(Context context) {
    Intent intent = new Intent(context, StockCardListActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    return intent;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (stockCardFragment != null) {
      stockCardFragment.onActivityResult(requestCode, resultCode, data);
    }
  }

  protected StockCardListFragment createFragment() {
    return new StockCardListFragment();
  }


  protected void addMenus(Menu menu) {
    menu.add(Menu.NONE, MENU_ID_ADD_NEW_DRUG, 100, getString(R.string.action_add_new_drug))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    menu.add(Menu.NONE, MENU_ID_ARCHIVE_LIST, 200, getString(R.string.action_navigate_archive))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_all_drugs_movements_history)) {
      menu.add(Menu.NONE, MENU_ID_MOVEMENT_HISTORY, 300,
          getString(R.string.menu_item_stock_movement_history))
          .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }
  }
}
