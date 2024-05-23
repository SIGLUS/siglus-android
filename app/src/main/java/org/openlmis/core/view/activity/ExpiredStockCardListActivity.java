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