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
import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.multidex.MultiDex;
import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.facebook.stetho.Stetho;
import java.io.File;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.onAdaptListener;
import net.danlew.android.joda.JodaTimeAndroid;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleanalytics.AnalyticsTracker;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManager;
import org.openlmis.core.network.NetworkSchedulerService;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.receiver.DebugReceiver;
import org.openlmis.core.receiver.NetworkChangeReceiver;
import org.openlmis.core.utils.AutoSizeUtil;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.FileUtil;
import roboguice.RoboGuice;


@SuppressWarnings({"squid:S2696", "squid:S5803"})
public class LMISApp extends Application {

  public static final String FACEBOOK_SO_NAME = "lib-main";

  private static LMISApp instance;

  private static final int JOB_ID_NETWORK_CHANGE = 123;

  private FragmentActivity topActivity = null;

  @Override
  public void onCreate() {
    super.onCreate();
    if (!isRoboUniTest()) {
      Stetho.initializeWithDefaults(this);
      SoLoader.init(this, false);
    }
    JodaTimeAndroid.init(this);
    RoboGuice.getInjector(this).injectMembersWithoutViews(this);
    SharedPreferenceMgr sharedPreferenceMgr = RoboGuice.getInjector(this).getInstance(SharedPreferenceMgr.class);
    //fix: Reset the syncing flag on illegal exit during initialization
    sharedPreferenceMgr.setIsSyncingLastYearStockCards(false);
    AnalyticsTracker.initialize(this);
    LMISApp.instance = this;
    registerNetWorkChangeListener();
    DebugReceiver.registerDebugBoardCastReceiver(this);
    configAutoSize();
    integrateFlipper();
    LmisSqliteOpenHelper.getInstance(this).checkDatabaseVersion();
    getCurrentActivity();
  }

  public void resetApp() throws LMISException {
    AnalyticsTracker.reset();
    AnalyticsTracker.initialize(this);

    LmisSqliteOpenHelper.closeHelper();
    LmisSqliteOpenHelper.getInstance(this).checkDatabaseVersion();
  }

  public boolean isRoboUniTest() {
    return "robolectric".equals(Build.FINGERPRINT);
  }

  public static LMISApp getInstance() {
    return instance;
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

  public FragmentActivity getTopActivity() {
    return topActivity;
  }

  public boolean getFeatureToggleFor(int id) {
    return getResources().getBoolean(id);
  }

  public LMISRestApi getRestApi() {
    return LMISRestManager.getInstance(this).getLmisRestApi();
  }

  public void wipeAppData() {
    File cache = getCacheDir();
    File appDir = new File(cache.getParent());
    if (appDir.exists()) {
      String[] fileList = appDir.list();
      if (fileList == null) {
        return;
      }

      for (String s : fileList) {
        if (!s.equals("lib") && !s.equals(FACEBOOK_SO_NAME)) {
          FileUtil.deleteDir(new File(appDir, s));
        }
      }
    }
  }

  public void killAppProcess() {
    android.os.Process.killProcess(android.os.Process.myPid());
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(base);
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
    JobInfo myJob = new JobInfo.Builder(JOB_ID_NETWORK_CHANGE,
        new ComponentName(this, NetworkSchedulerService.class))
        .setMinimumLatency(1000)
        .setOverrideDeadline(2000)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setPersisted(true)
        .build();

    JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    jobScheduler.schedule(myJob);
  }

  @SuppressWarnings("squid:S1874")
  private void registerNetWorkListener() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    registerReceiver(networkChangeReceiver, filter);
  }

  private void configAutoSize() {
    AutoSizeConfig.getInstance().setCustomFragment(true);
    AutoSizeConfig.getInstance().setOnAdaptListener(new onAdaptListener() {
      @Override
      public void onAdaptBefore(Object target, Activity activity) {
        AutoSizeUtil.resetScreenSize(activity);
      }

      @Override
      public void onAdaptAfter(Object target, Activity activity) {
        // do nothing
      }
    });
  }

  private void integrateFlipper() {
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_flipper) && FlipperUtils.shouldEnableFlipper(this)) {
      final FlipperClient client = AndroidFlipperClient.getInstance(this);
      client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()));
      client.addPlugin(new DatabasesFlipperPlugin(this));
      client.addPlugin(new SharedPreferencesFlipperPlugin(this, "LMISPreference"));
      client.start();
    }
  }

  private void getCurrentActivity() {
    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        //do nothing
      }

      @Override
      public void onActivityStarted(@NonNull Activity activity) {
        //do nothing
      }

      @Override
      public void onActivityResumed(@NonNull Activity activity) {
        if (activity instanceof FragmentActivity) {
          topActivity = (FragmentActivity) activity;
        }
      }

      @Override
      public void onActivityPaused(@NonNull Activity activity) {
        //do nothing
      }

      @Override
      public void onActivityStopped(@NonNull Activity activity) {
        //do nothing
      }

      @Override
      public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        //do nothing
      }

      @Override
      public void onActivityDestroyed(@NonNull Activity activity) {
        //do nothing
      }
    });
  }


}
