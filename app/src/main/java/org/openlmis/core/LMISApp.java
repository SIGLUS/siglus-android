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

import android.annotation.TargetApi;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

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
import org.openlmis.core.network.NetworkSchedulerService;
import org.openlmis.core.receiver.NetworkChangeReceiver;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;

public class LMISApp extends Application {
    private static final String TAG = LMISApp.class.getSimpleName();

    private static LMISApp instance;

    public static long lastOperateTime = 0L;
    private final int facilityCustomDimensionKey = 1;

    private static final int JOB_ID_NETWORK_CHANGE = 123;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!isRoboUniTest()) {
            Stetho.initializeWithDefaults(this);
        }
        JodaTimeAndroid.init(this);
        RoboGuice.getInjector(this).injectMembersWithoutViews(this);
        RoboGuice.getInjector(this).getInstance(SharedPreferenceMgr.class);
        setupAppCenter();
        setupGoogleAnalytics();

        instance = this;
        registerNetWorkChangeListener();
    }

    // Test case throw IO error
    private boolean isRoboUniTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }

    private void registerNetWorkChangeListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startScheduleJob();
        } else {
            registerNetWorkListener();
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void startScheduleJob() {
        JobInfo myJob = new JobInfo.Builder(JOB_ID_NETWORK_CHANGE, new ComponentName(this, NetworkSchedulerService.class))
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(myJob);
    }

    private void registerNetWorkListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, filter);
    }

    protected void setupGoogleAnalytics() {
        AnalyticsTrackers.initialize(this);
    }

    public static LMISApp getInstance() {
        return instance;
    }

    private void setupAppCenter() {
        AppCenter.start(this, getString(R.string.appcenter_app_key), Analytics.class, Crashes.class);
        AppCenter.setEnabled(true);
        Analytics.setEnabled(true);
    }

    public long getCurrentTimeMillis() {
        return DateUtil.getCurrentDate().getTime();
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

    public void logErrorToFirebase(LMISException exception) {
        Analytics.isEnabled().thenAccept(enable -> {
            final StackTraceElement[] traceElements = exception.getStackTrace();
            if (enable && (traceElements.length > 0)) {
                Map<String, String> properties = new HashMap<>(traceElements.length);

                for (int i = traceElements.length - 1; i >= 0; i--) {
                    properties.put(Integer.toString(i), traceElements[i].toString());
                }
                AppCenter.setUserId(UserInfoMgr.getInstance().getFacilityName());
                Analytics.trackEvent(exception.getMsg(), properties);
            }
        });
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
