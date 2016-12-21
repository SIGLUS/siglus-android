package org.openlmis.core.service;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.utils.Constants;

public class TrainingSyncAdapter {
    @Inject
    SyncUpManager syncUpManager;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    Context context;

    public String onPerformSync() {
        context = LMISApp.getContext();
        triggerFakeSync();
        return "immediate sync up requested";
    }

    private void triggerFakeSync() {
        sendSyncStartBroadcast();

        boolean isFakeSyncRnrSuccessful = syncUpManager.fakeSyncRnr();
        if (isFakeSyncRnrSuccessful) {
            sharedPreferenceMgr.setRnrLastSyncTime();
        }

        boolean isFakeSyncStockSuccessful = syncUpManager.fakeSyncStockCards();
        if (isFakeSyncStockSuccessful) {
            sharedPreferenceMgr.setStockLastSyncTime();
        }

        syncUpManager.fakeSyncUpUnSyncedStockCardCodes();
        if (!sharedPreferenceMgr.hasSyncedVersion()) {
            sharedPreferenceMgr.setSyncedVersion(true);
        }
        syncUpManager.fakeSyncUpCmms();
        syncUpManager.fakeSyncRapidTestForms();
        sendSyncFinishedBroadcast();
    }

    private void sendSyncStartBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_START_SYNC_DATA);
        context.sendBroadcast(intent);
    }

    private void sendSyncFinishedBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_FILTER_FINISH_SYNC_DATA);
        context.sendBroadcast(intent);
    }
}
