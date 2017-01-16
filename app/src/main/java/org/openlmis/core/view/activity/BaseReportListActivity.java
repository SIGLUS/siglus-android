package org.openlmis.core.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;

import org.openlmis.core.utils.Constants;

public abstract class BaseReportListActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
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
    protected void onDestroy() {
        unregisterReceiver(syncReceiver);
        super.onDestroy();
    }
}
