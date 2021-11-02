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

package org.openlmis.core.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.event.SyncStatusEvent;
import org.openlmis.core.event.SyncStatusEvent.SyncStatus;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.User;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import roboguice.RoboGuice;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

  private static final String TAG = SyncAdapter.class.getSimpleName();

  @Inject
  SyncUpManager syncUpManager;

  @Inject
  SyncDownManager syncDownManager;

  @Inject
  UpgradeManager upgradeManager;

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;

  @Inject
  DirtyDataManager dirtyDataManager;

  Context context;

  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    RoboGuice.getInjector(context).injectMembers(this);
    this.context = context;
  }

  @Override
  public void onPerformSync(Account account, Bundle extras, String authority,
      ContentProviderClient provider, SyncResult syncResult) {
    User user = UserInfoMgr.getInstance().getUser();
    if (user == null) {
      Log.d(TAG, "No user login, skip sync....");
      return;
    }
    Log.d(TAG, "===> Syncing Data to server");
    if (shouldCorrectData(extras) && sharedPreferenceMgr.shouldStartHourlyDirtyDataCheck()) {
      List<StockCard> deleteStockCards = dirtyDataManager.correctData();
      if (!CollectionUtils.isEmpty(deleteStockCards)) {
        sendDeletedProductBroadcast();
      }
    }
    upgradeManager.triggerUpgrade();
    triggerSync();
  }

  private boolean shouldCorrectData(Bundle extras) {
    return extras != null && extras.getBoolean(Constants.IS_USER_TRIGGERED_SYCED);
  }

  private void triggerSync() {
    sendSyncStartBroadcast();
    syncDownManager.syncDownServerData();
    syncUpManager.syncUpData();
  }


  private void sendSyncStartBroadcast() {
    EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.START));
  }


  private void sendDeletedProductBroadcast() {
    Intent intent = new Intent(Constants.INTENT_FILTER_DELETED_PRODUCT);
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
}
