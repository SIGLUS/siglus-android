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
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;

import java.util.Date;

import roboguice.RoboGuice;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    SyncManager syncManager;

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
        if(user == null){
            Log.d("SyncAdapter", "No user login, skip sync....");
            return;
        }
        Log.d("SyncAdapter", "===> Syncing Data to server");

        boolean rnRSynced = syncManager.syncRnr();

        boolean stockCardSynced = true;
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_sync_stock_card_279)){
            stockCardSynced = syncManager.syncStockCards();
        }

        if (rnRSynced && stockCardSynced) {
            recordLastSyncedTime();
        }
    }

    private void recordLastSyncedTime(){
        sharedPreferenceMgr.getPreference().edit().putLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME, new Date().getTime()).apply();
    }

}
