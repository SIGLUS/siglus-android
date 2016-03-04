package org.openlmis.core.service;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import org.openlmis.core.BuildConfig;
import org.openlmis.core.R;

public final class AnalyticsTrackers {

    private static AnalyticsTrackers sInstance;

    public static synchronized void initialize(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("Extra call to initialize analytics trackers");
        }

        sInstance = new AnalyticsTrackers(context);
    }

    public static synchronized AnalyticsTrackers getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Call initialize() before getInstance()");
        }

        return sInstance;
    }

    private Tracker mTracker;

    /**
     * Don't instantiate directly - use {@link #getInstance()} instead.
     */
    private AnalyticsTrackers(Context context) {
        Context mContext = context.getApplicationContext();
        GoogleAnalytics.getInstance(mContext).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        mTracker = GoogleAnalytics.getInstance(mContext).newTracker(mContext.getString(R.string.ga_trackingId));
        mTracker.setSessionTimeout(300);
        mTracker.setAppVersion(BuildConfig.VERSION_NAME);
        mTracker.setAppId(BuildConfig.APPLICATION_ID);
    }

    public synchronized Tracker getDefault() {
        return mTracker;
    }

    public void sendScreenToGoogleAnalytics(String screenName, String facilityName) {
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder()
                .setCustomDimension(1, facilityName)
                .build());
    }

}
