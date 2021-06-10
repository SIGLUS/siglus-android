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

package org.openlmis.core.network;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;
import org.openlmis.core.LMISApp;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.service.SyncService;
import roboguice.RoboGuice;

@TargetApi(Build.VERSION_CODES.N)
public class NetworkSchedulerService extends JobService {

  private static final String TAG = NetworkSchedulerService.class.getSimpleName();
  private ConnectivityManager mConnectivityManager;
  private ConnectivityNetworkCallback mNetworkCallback;
  private SyncService syncService;
  private NetworkCapabilities mNetworkCapabilities = null;

  @Override
  public boolean onStartJob(JobParameters params) {
    mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
    return true;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "onCreate");
    mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    syncService = RoboGuice.getInjector(getApplicationContext()).getInstance(SyncService.class);
    mNetworkCallback = new ConnectivityNetworkCallback();

  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "onStartCommand");
    return START_NOT_STICKY;
  }


  @Override
  public boolean onStopJob(JobParameters params) {
    mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
    jobFinished(params, false);
    return true;
  }

  private void syncImmediately() {
    LMISApp.getInstance().trackEvent(TrackerCategories.NETWORK, TrackerActions.NETWORK_CONNECTED);
    syncService.requestSyncImmediatelyByTask();
    syncService.kickOff();
  }

  private void shutDownImmediately() {
    Log.d(TAG, "there is no internet connection in network receiver");
    Log.d(TAG, "network disconnect, stop sync service...");
    LMISApp.getInstance()
        .trackEvent(TrackerCategories.NETWORK, TrackerActions.NETWORK_DISCONNECTED);
    syncService.shutDown();
  }

  private class ConnectivityNetworkCallback extends ConnectivityManager.NetworkCallback {

    @Override
    public void onAvailable(Network network) {
      mNetworkCapabilities = mConnectivityManager.getNetworkCapabilities(network);
      if (mNetworkCapabilities != null
          && mNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
          && mNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
        syncImmediately();
      } else {
        shutDownImmediately();
      }
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
      shutDownImmediately();
    }

    @Override
    public void onLost(Network network) {
      shutDownImmediately();
    }
  }
}
