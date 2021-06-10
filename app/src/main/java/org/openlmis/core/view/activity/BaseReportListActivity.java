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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.openlmis.core.utils.Constants;

public abstract class BaseReportListActivity extends BaseActivity {

  @Override
  protected void onStart() {
    super.onStart();
    registerRnrSyncReceiver();
  }

  private void registerRnrSyncReceiver() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Constants.INTENT_FILTER_FINISH_SYNC_DATA);
    LocalBroadcastManager.getInstance(this).registerReceiver(syncReceiver, filter);
  }

  BroadcastReceiver syncReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      loadForms();
    }
  };

  protected abstract void loadForms();

  @Override
  protected void onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver);
    super.onStop();
  }
}
