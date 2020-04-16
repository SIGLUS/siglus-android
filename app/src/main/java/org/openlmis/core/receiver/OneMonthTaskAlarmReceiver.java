package org.openlmis.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.openlmis.core.service.CheckMovementIntentService;

public class OneMonthTaskAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = OneMonthTaskAlarmReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        Log.e(TAG, "onReceive: ");
        CheckMovementIntentService.startActionCheckMovement(context);
    }
}
