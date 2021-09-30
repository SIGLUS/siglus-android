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

package org.openlmis.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.event.DebugInitialInventoryEvent;
import org.openlmis.core.event.DebugPhysicalInventoryEvent;

/**
 * 1. quickly complete initial inventory:
 * eg: adb shell am broadcast -a org.openlmis.core.debug.initial_inventory
 * --ei basicProduct 10 --ei nonBasicProduct 10 --ei lotPerProduct 10
 *
 * <p>basicProduct: the basic product amount which need add lot.
 * nonBasicProduct: the non basic product amount which need
 * add lot. lotPerProduct: lot amount per product
 *
 * <p>2. quickly complete physical inventory:
 * eg: adb shell am broadcast -a org.openlmis.core.debug.physical_inventory
 */
public class DebugReceiver extends BroadcastReceiver {

  private static final String ACTION_INITIAL_INVENTORY = "org.openlmis.core.debug.initial_inventory";
  private static final String ACTION_PHYSICAL_INVENTORY = "org.openlmis.core.debug.physical_inventory";
  private static final String PARAM_BASIC_PRODUCT_AVAILABLE = "basicProduct";
  private static final String PARAM_NON_BASIC_PRODUCT_AVAILABLE = "nonBasicProduct";
  private static final String PARAM_LOT_AMOUNT_PER_PRODUCT = "lotPerProduct";

  public static void registerDebugBoardCastReceiver(Context context) {
    if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_debug)) {
      return;
    }
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_INITIAL_INVENTORY);
    filter.addAction(ACTION_PHYSICAL_INVENTORY);
    context.registerReceiver(new DebugReceiver(), filter);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    switch (intent.getAction()) {
      case ACTION_INITIAL_INVENTORY:
        int basicProduct = intent.getIntExtra(PARAM_BASIC_PRODUCT_AVAILABLE, 0);
        int nonBasicProduct = intent.getIntExtra(PARAM_NON_BASIC_PRODUCT_AVAILABLE, 0);
        int lotPerProduct = intent.getIntExtra(PARAM_LOT_AMOUNT_PER_PRODUCT, 0);
        DebugInitialInventoryEvent event = new DebugInitialInventoryEvent(basicProduct, nonBasicProduct, lotPerProduct);
        Log.d("DebugReceiver", event.toString());
        EventBus.getDefault().post(event);
        break;
      case ACTION_PHYSICAL_INVENTORY:
        Log.d("DebugReceiver", ACTION_PHYSICAL_INVENTORY);
        EventBus.getDefault().post(new DebugPhysicalInventoryEvent());
        break;
      default:
        // do nothing
        break;
    }
  }
}
