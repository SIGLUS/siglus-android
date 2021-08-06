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
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.presenter.StockCardListPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.fragment.StockCardListFragment;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_stockcard_list)
public class StockCardListActivity extends SearchBarActivity {

  protected static final int MENU_ID_ADD_NEW_DRUG = 100;
  protected static final int MENU_ID_ARCHIVE_LIST = 200;
  private static final int MENU_ID_MOVEMENT_HISTORY = 300;
  private static final int MENU_ID_BULK_ISSUES = 400;
  private static final int MENU_ID_BULK_ENTRIES = 500;

  protected StockCardListFragment stockCardFragment;

  @InjectPresenter(StockCardListPresenter.class)
  private StockCardListPresenter presenter;

  public static Intent getIntentToMe(Context context) {
    Intent intent = new Intent(context, StockCardListActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    return intent;
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.STOCK_CARD_OVERVIEW_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      return;
    }
    stockCardFragment = createFragment();
    getSupportFragmentManager().beginTransaction().replace(R.id.stock_card_container, stockCardFragment).commit();
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
        return true;
      case MENU_ID_BULK_ENTRIES:
        startActivityForResult(new Intent(LMISApp.getContext(), BulkEntriesActivity.class),
            Constants.REQUEST_FROM_STOCK_LIST_PAGE);
        return true;
      case MENU_ID_BULK_ISSUES:
        Intent intent = new Intent(LMISApp.getContext(),
            presenter.hasBulkIssueDraft() ? BulkIssueActivity.class : BulkIssueChooseDestinationActivity.class);
        startActivityForResult(intent, Constants.REQUEST_FROM_STOCK_LIST_PAGE);
        return true;
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

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
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
      menu.add(Menu.NONE, MENU_ID_BULK_ISSUES, 400, getString(R.string.action_bulk_issues))
          .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
      menu.add(Menu.NONE, MENU_ID_BULK_ENTRIES, 500, getString(R.string.action_bulk_entries))
          .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }
  }
}
