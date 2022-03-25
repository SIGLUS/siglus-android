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
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.AnalyticsTracker;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.googleanalytics.TrackerCategories;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.InternetCheckListener;
import org.openlmis.core.service.SyncService;
import roboguice.RoboGuice;

public class NetworkChangeReceiver extends BroadcastReceiver {

  private static final String TAG = NetworkChangeReceiver.class.getSimpleName();

  @Inject
  InternetCheck internetCheck;

  @Override
  public void onReceive(Context context, Intent intent) {
    SyncService syncService = RoboGuice.getInjector(context).getInstance(SyncService.class);
    if (internetCheck != null && !LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      internetCheck.check(synchronizeListener(syncService));
    }
  }

  protected InternetCheckListener synchronizeListener(final SyncService syncService) {
    return internet -> {
      if (internet) {
        Log.d(TAG, "network connected, start sync service...");
        AnalyticsTracker.getInstance().trackEvent(TrackerCategories.NETWORK, TrackerActions.NETWORK_CONNECTED);
        syncService.requestSyncImmediatelyByTask();
        syncService.kickOff();
      } else {
        Log.d(TAG, "there is no internet connection in network receiver");
        AnalyticsTracker.getInstance().trackEvent(TrackerCategories.NETWORK, TrackerActions.NETWORK_DISCONNECTED);
        syncService.shutDown();
      }
    };
  }
}
