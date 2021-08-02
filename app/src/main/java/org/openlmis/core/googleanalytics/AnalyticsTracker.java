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
import androidx.annotation.NonNull;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;

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

  /**
   * Don't instantiate directly - use {@link #getInstance()} instead.
   */
  private AnalyticsTracker(Application application) {
    AppCenter.start(application,
        application.getResources().getString(R.string.appcenter_app_key), Analytics.class, Crashes.class);
    AppCenter.setEnabled(true);
    Analytics.setEnabled(true);
  }

  public void setUserInfo(@NonNull String userName) {
    AppCenter.isEnabled().thenAccept(enable -> {
      if (Boolean.FALSE.equals(enable)) {
        AppCenter.setUserId(userName);
      }
    });
  }

  public void traceError(LMISException exception) {
    traceError(exception, null);
  }

  public void traceError(LMISException exception, Map<String, String> properties) {
    Crashes.isEnabled().thenAccept(enable -> {
      if (Boolean.TRUE.equals(enable)) {
        HashMap<String, String> params = new HashMap<>();
        params.put("errorMsg", exception.getMsg());
        if (properties != null) {
          params.putAll(properties);
        }
        AppCenter.setUserId(UserInfoMgr.getInstance().getUserNameForAppCenter());
        Crashes.trackError(exception, params, null);
      }
    });
  }

  public void trackEvent(TrackerCategories category, TrackerActions action) {
    HashMap<String, String> bundle = new HashMap<>();
    bundle.put("category", category.getString());
    bundle.put("actionName", action.getString());
    bundle.put("processDate", new DateTime(LMISApp.getInstance().getCurrentTimeMillis()).toInstant().toString());
    Analytics.isEnabled().thenAccept(enable -> {
      if (Boolean.TRUE.equals(enable)) {
        AppCenter.setUserId(UserInfoMgr.getInstance().getUserNameForAppCenter());
        Analytics.trackEvent("UserAction", bundle);
      }
    });
  }

  public void trackScreen(ScreenName screenName) {
    HashMap<String, String> bundle = new HashMap<>();
    bundle.put("screenName", screenName.getName());
    bundle.put("processDate", new DateTime(LMISApp.getInstance().getCurrentTimeMillis()).toInstant().toString());
    Analytics.isEnabled().thenAccept(enable -> {
      if (Boolean.TRUE.equals(enable)) {
        AppCenter.setUserId(UserInfoMgr.getInstance().getUserNameForAppCenter());
        Analytics.trackEvent("ScreenName", bundle);
      }
    });
  }
}
