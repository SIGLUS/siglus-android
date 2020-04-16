package org.openlmis.core.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.Constants;

import java.util.List;

import roboguice.RoboGuice;

public class CheckMovementIntentService extends IntentService {
    private static final String TAG = CheckMovementIntentService.class.getSimpleName();
    @Inject
    DirtyDataManager dirtyDataManager;


    public CheckMovementIntentService() {
        super("CheckMovementIntentService constructor");
    }

    public static void startActionCheckMovement(Context context) {
        Intent intent = new Intent(context, CheckMovementIntentService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "onHandleIntent:");
        List<StockCard> stockCardList = dirtyDataManager.scanAllStockMovements();
        if (stockCardList != null && !stockCardList.isEmpty()) {
            sendIntent();
        }
    }

    private void sendIntent() {
        Intent intent = new Intent(Constants.INTENT_FILTER_DELETED_PRODUCT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
