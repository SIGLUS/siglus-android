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

import com.google.inject.Inject;

import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.utils.Constants;

import java.util.Date;

import roboguice.RoboGuice;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    SyncUpManager syncUpManager;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    Context context;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        RoboGuice.getInjector(context).injectMembers(this);
        this.context = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        User user = UserInfoMgr.getInstance().getUser();
        if (user == null) {
            Log.d("SyncAdapter", "No user login, skip sync....");
            return;
        }
        Log.d("SyncAdapter", "===> Syncing Data to server");

        recordSyncedTime(syncUpManager.syncRnr(), syncUpManager.syncStockCards());

        syncUpManager.syncAppVersion();
    }

    private void recordSyncedTime(boolean rnRSynced, boolean stockCardSynced) {
        if (rnRSynced) {
            recordRnrFormLastSyncedTime();
        }

        if (stockCardSynced) {
            recordStockCardLastSyncedTime();
        }

        sendSyncedTimeBroadcast();
    }

    private void sendSyncedTimeBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_SET_SYNCED_TIME);
        context.sendBroadcast(intent);
    }

    private void recordRnrFormLastSyncedTime() {
        sharedPreferenceMgr.getPreference().edit().putLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_RNR_FORM, new Date().getTime()).apply();
    }

    private void recordStockCardLastSyncedTime() {
        sharedPreferenceMgr.getPreference().edit().putLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_STOCKCARD, new Date().getTime()).apply();
    }

}
