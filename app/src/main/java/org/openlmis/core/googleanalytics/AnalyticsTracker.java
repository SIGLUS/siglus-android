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

package org.openlmis.core.googleanalytics;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.openlmis.core.BuildConfig;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;

public final class AnalyticsTracker {

  private static AnalyticsTracker sInstance;

  public static synchronized void initialize(Application application) {
    if (sInstance != null) {
      return;
    }
    sInstance = new AnalyticsTracker(application);
  }

  public static synchronized AnalyticsTracker getInstance() {
    if (sInstance == null) {
      throw new IllegalStateException("Call initialize() before getInstance()");
    }
    return sInstance;
  }

  private final FirebaseAnalytics firebaseTracker;

  /**
   * Don't instantiate directly - use {@link #getInstance()} instead.
   */
  private AnalyticsTracker(Application application) {
    Context context = application.getApplicationContext();

    // init google analytics
    FirebaseApp.initializeApp(context);
    firebaseTracker = FirebaseAnalytics.getInstance(context);
    firebaseTracker.setSessionTimeoutDuration(300);
    firebaseTracker.setUserProperty("AppVersion", BuildConfig.VERSION_NAME);

    // init app center analytics
    AppCenter.start(application,
        application.getResources().getString(R.string.appcenter_app_key), Analytics.class, Crashes.class);
    AppCenter.setEnabled(true);
    Analytics.setEnabled(true);
  }

  public synchronized FirebaseAnalytics getGoogleAnalyticsTracker() {
    return firebaseTracker;
  }

  public void setUserInfo(@NonNull User user) {
    AppCenter.isEnabled().thenAccept(enable -> {
      if (Boolean.TRUE.equals(enable)) {
        AppCenter.setUserId(user.getFacilityName());
      }
    });
    firebaseTracker.setUserId(user.getFacilityName());
    FirebaseCrashlytics.getInstance().setUserId(user.getFacilityName());
  }

  public void traceError(LMISException exception) {
    Analytics.isEnabled().thenAccept(enable -> {
      final StackTraceElement[] traceElements = exception.getStackTrace();
      if (Boolean.TRUE.equals(enable) && (traceElements.length > 0)) {
        Map<String, String> properties = new HashMap<>(traceElements.length);
        for (int i = traceElements.length - 1; i >= 0; i--) {
          properties.put(Integer.toString(i), traceElements[i].toString());
        }
        AppCenter.setUserId(UserInfoMgr.getInstance().getFacilityName());
        Analytics.trackEvent(exception.getMsg(), properties);
      }
    });
    FirebaseCrashlytics.getInstance().recordException(exception);
  }

  public void trackEvent(TrackerCategories category, TrackerActions action) {
    Bundle bundle = new Bundle();
    bundle.putString("Category", category.getString());
    bundle.putString("ActionName", action.getString());
    bundle.putString("ProcessDate", new DateTime(LMISApp.getInstance().getCurrentTimeMillis()).toInstant().toString());
    firebaseTracker.logEvent("UserAction", bundle);
  }

  public void trackScreen(ScreenName screenName) {
    Bundle bundle = new Bundle();
    bundle.putString("ScreenName", screenName.getName());
    bundle.putString("ProcessDate", new DateTime(LMISApp.getInstance().getCurrentTimeMillis()).toInstant().toString());
    firebaseTracker.logEvent("ScreenName", bundle);
  }
}
