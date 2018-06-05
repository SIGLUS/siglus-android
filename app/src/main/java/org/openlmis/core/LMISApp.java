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

package org.openlmis.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.danlew.android.joda.JodaTimeAndroid;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.AnalyticsTrackers;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManager;
import org.openlmis.core.network.NetworkConnectionManager;
import org.openlmis.core.utils.FileUtil;

import java.io.File;

import io.fabric.sdk.android.Fabric;
import roboguice.RoboGuice;

public class LMISApp extends Application {

    private static LMISApp instance;

    public static long lastOperateTime = 0L;
    private static String TAG = "123";
    private final int facilityCustomDimensionKey = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        JodaTimeAndroid.init(this);
        RoboGuice.getInjector(this).injectMembersWithoutViews(this);
        RoboGuice.getInjector(this).getInstance(SharedPreferenceMgr.class);
        if(!BuildConfig.DEBUG) {
            setupFabric();
        }
        setupGoogleAnalytics();

        instance = this;
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d(TAG,"onActivityCreated: " + activity.getLocalClassName());
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d(TAG,"onActivityStarted: " + activity.getLocalClassName());
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.d(TAG,"onActivityResumed: " + activity.getLocalClassName());
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d(TAG,"onActivityPaused: " + activity.getLocalClassName());
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d(TAG, "onActivityStopped: " + activity.getLocalClassName());
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG,"onActivityDestroyed: " + activity.getLocalClassName());
            }
        });
    }

    protected void setupGoogleAnalytics() {
        AnalyticsTrackers.initialize(this);
    }

    public static LMISApp getInstance() {
        return instance;
    }

    protected void setupFabric() {
        Fabric.with(this, new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build());
    }

    public boolean isConnectionAvailable() {
        return NetworkConnectionManager.isConnectionAvailable(instance);
    }

    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MovementReasonManager.getInstance().refresh();
    }

    public boolean getFeatureToggleFor(int id) {
        return getResources().getBoolean(id);
    }

    public void logErrorOnFabric(LMISException exception) {
        Crashlytics.logException(exception);
    }

    public LMISRestApi getRestApi() {
        return LMISRestManager.getInstance(this).getLmisRestApi();
    }

    public void trackScreen(ScreenName screenName) {
        Tracker mTracker = AnalyticsTrackers.getInstance().getDefault();
        mTracker.setScreenName(screenName.getScreenName());
        mTracker.send(new HitBuilders.ScreenViewBuilder()
                .setCustomDimension(facilityCustomDimensionKey, getFacilityNameForGA())
                .build());
    }

    public void trackEvent(TrackerCategories category, TrackerActions action) {
        Tracker mTracker = AnalyticsTrackers.getInstance().getDefault();
        mTracker.send(new HitBuilders.EventBuilder(category.getString(), action.getString())
                .setCustomDimension(facilityCustomDimensionKey, getFacilityNameForGA())
                .build());
    }

    private String getFacilityNameForGA() {
        String facilityName = UserInfoMgr.getInstance().getFacilityName();
        return TextUtils.isEmpty(facilityName)
                ? SharedPreferenceMgr.getInstance().getCurrentUserFacility() : facilityName;
    }

    public void wipeAppData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (new File(getCacheDir().getParent()).exists()) {
            for (String s : appDir.list()) {
                if (!s.equals("lib")) {
                    FileUtil.deleteDir(new File(appDir, s));
                }
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public boolean isQAEnabled() {
        return SharedPreferenceMgr.getInstance().isQaDebugEnabled();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
