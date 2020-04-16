package org.openlmis.core.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.Constants;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CheckMovementJobService extends JobService {
    private static final String TAG = CheckMovementJobService.class.getSimpleName();
    private JobParameters mParams;
    DirtyDataManager dirtyDataManager = new DirtyDataManager();

    @Override
    public boolean onStartJob(JobParameters params) {
        mParams = params;
        new ScanTask().execute();
        Log.e(TAG, "onStartJob: " + dirtyDataManager);
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: " + dirtyDataManager);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    private class ScanTask extends AsyncTask<Void, Void, List<StockCard>> {
        @Override
        protected List<StockCard> doInBackground(Void... voids) {
            return dirtyDataManager.scanAllStockMovements();
        }

        @Override
        protected void onPostExecute(List<StockCard> stockCardList) {
            super.onPostExecute(stockCardList);
            jobFinished(mParams, false);
            if (stockCardList != null && !stockCardList.isEmpty()) {
                sendIntent();
            }
        }
    }

    private void sendIntent() {
        Intent intent = new Intent(Constants.INTENT_FILTER_DELETED_PRODUCT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
