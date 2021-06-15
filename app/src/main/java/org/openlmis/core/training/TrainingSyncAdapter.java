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

package org.openlmis.core.training;

import android.content.Context;
import android.util.Log;
import com.google.inject.Inject;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.LMISApp;
import org.openlmis.core.event.SyncStatusEvent;
import org.openlmis.core.event.SyncStatusEvent.SyncStatus;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.service.SyncUpManager;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TrainingSyncAdapter {

  @Inject
  SyncUpManager syncUpManager;

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;

  Context context;

  protected String onPerformSync() {
    context = LMISApp.getContext();
    triggerFakeSync();
    return "immediate sync up requested";
  }

  private void triggerFakeSync() {
    sendSyncStartBroadcast();

    boolean isFakeSyncRnrSuccessful = syncUpManager.fakeSyncRnr();
    if (isFakeSyncRnrSuccessful) {
      sharedPreferenceMgr.setRnrLastSyncTime();
    }

    boolean isFakeSyncStockSuccessful = syncUpManager.fakeSyncStockCards();
    if (isFakeSyncStockSuccessful) {
      sharedPreferenceMgr.setStockLastSyncTime();
    }

    syncUpManager.fakeSyncUpUnSyncedStockCardCodes();
    if (!sharedPreferenceMgr.hasSyncedVersion()) {
      sharedPreferenceMgr.setSyncedVersion(true);
    }
    syncUpManager.fakeSyncUpCmms();
    syncUpManager.fakeSyncRapidTestForms();
    sendSyncFinishedBroadcast();
  }

  private void sendSyncStartBroadcast() {
    EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.START));

  }

  private void sendSyncFinishedBroadcast() {
    EventBus.getDefault().post(new SyncStatusEvent(SyncStatus.FINISH));
  }

  public void requestSync() {
    Single.create((Single.OnSubscribe<String>) singleSubscriber -> singleSubscriber
        .onSuccess(onPerformSync()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(message -> Log.d("training sync", message));
  }
}
