package org.openlmis.core.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

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
        registerReceiver(syncReceiver, filter);
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
        unregisterReceiver(syncReceiver);
        super.onStop();
    }
}
