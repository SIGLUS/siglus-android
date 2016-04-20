package org.openlmis.core.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.openlmis.core.LMISApp;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;

public class ShutDownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            Log.d("ShutDownReceiver: ", "shut down");
            LMISApp.getInstance().trackEvent(TrackerCategories.SWITCH, TrackerActions.SwitchPowerOff);
        }
    }
}
