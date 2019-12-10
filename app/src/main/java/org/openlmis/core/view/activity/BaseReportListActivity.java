package org.openlmis.core.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import org.openlmis.core.utils.Constants;

public abstract class BaseReportListActivity extends BaseActivity {
    @Override
    protected void onStart() {
        super.onStart();
        registerRnrSyncReceiver();
    }

    private void registerRnrSyncReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.INTENT_FILTER_FINISH_SYNC_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(syncReceiver, filter);
    }

    BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadForms();
        }
    };

    protected abstract void loadForms();

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver);
        super.onStop();
    }
}
