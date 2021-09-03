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

package org.openlmis.core.manager;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.BuildConfig;
import org.openlmis.core.googleanalytics.AnalyticsTracker;
import org.openlmis.core.model.User;

public final class UserInfoMgr {

  /**
   * app center max user id max length
   * see {@link com.microsoft.appcenter.utils.context.UserIdContext#USER_ID_APP_CENTER_MAX_LENGTH}
   */
  private static final int USER_NAME_MAX_LENGTH = 256;
  private static UserInfoMgr mInstance;
  private User user;

  private UserInfoMgr() {
  }

  public static UserInfoMgr getInstance() {
    if (mInstance == null) {
      mInstance = new UserInfoMgr();
    }
    return mInstance;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
    SharedPreferenceMgr.getInstance().setCurrentUserFacility(user.getFacilityName());
    SharedPreferenceMgr.getInstance().setLastLoginUser(user.getUsername());
    SharedPreferenceMgr.getInstance().setUserFacilityCode(user.getFacilityCode());
    AnalyticsTracker.getInstance().setUserInfo(getUserNameForAppCenter());
  }

  public String getVersion() {
    return BuildConfig.VERSION_NAME;
  }

  public String getFacilityCode() {
    return user == null ? "" : user.getFacilityCode();
  }

  public String getFacilityName() {
    return user == null ? "" : user.getFacilityName();
  }

  public String getUserNameForAppCenter() {
    String userName;
    String lastUserFacilityName = SharedPreferenceMgr.getInstance().getCurrentUserFacility();
    String lastUserName = SharedPreferenceMgr.getInstance().getLastLoginUser();
    if (user != null) {
      userName = String.format("[%s] %s", user.getFacilityName(), user.getUsername());
    } else if (StringUtils.isNotBlank(lastUserFacilityName) && StringUtils.isNotBlank(lastUserName)) {
      userName = String.format("[%s] %s", lastUserFacilityName, lastUserName);
    } else {
      return "";
    }
    return userName.substring(0, Math.min(userName.length(), USER_NAME_MAX_LENGTH));
  }
}
