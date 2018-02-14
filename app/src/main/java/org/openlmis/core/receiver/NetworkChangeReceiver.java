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
import android.util.Log;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.service.SyncService;

import roboguice.RoboGuice;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Inject
    InternetCheck internetCheck;

    @Override
    public void onReceive(Context context, Intent intent) {
        SyncService syncService = RoboGuice.getInjector(context).getInstance(SyncService.class);
        if(internetCheck!=null) {
            internetCheck.execute(synchronizeListener(syncService));
        }
    }

    private InternetCheck.Callback synchronizeListener(final SyncService syncService) {

        return new InternetCheck.Callback() {
            @Override
            public void launchResponse(Boolean internet) {
                if (internet) {
                    Log.d("NetworkChangeReceiver :", "network connected, start sync service...");
                    LMISApp.getInstance().trackEvent(TrackerCategories.NETWORK, TrackerActions.NetworkConnected);
                    syncService.requestSyncImmediately();
                    syncService.kickOff();
                } else {
                    Log.d("Internet Connection", "there is no internet connection in network receiver");
                    Log.d("NetworkChangeReceiver :", "network disconnect, stop sync service...");
                    LMISApp.getInstance().trackEvent(TrackerCategories.NETWORK, TrackerActions.NetworkDisconnected);
                    syncService.shutDown();
                }
            }
        };
    }


}
