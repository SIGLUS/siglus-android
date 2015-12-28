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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.openlmis.core.network.NetworkConnectionManager;

import roboguice.RoboGuice;

public class NetworkChangeListener extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        SyncUpManager manager = RoboGuice.getInjector(context).getInstance(SyncUpManager.class);
        if (NetworkConnectionManager.isConnectionAvailable(context)){
            Log.d("NetworkChangeListener :", "network connected, start sync service...");
            manager.requestSyncImmediately();
            manager.kickOff();
        } else {
            Log.d("NetworkChangeListener :", "network disconnect, stop sync service...");
            manager.shutDown();
        }
    }
}
