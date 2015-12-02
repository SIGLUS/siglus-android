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


import com.crashlytics.android.Crashlytics;

import org.openlmis.core.BuildConfig;
import org.openlmis.core.model.User;

import io.fabric.sdk.android.Fabric;

public final class UserInfoMgr {
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
        if (Fabric.isInitialized()) {
            Crashlytics.setUserIdentifier(user.getFacilityName());
        }
        this.user = user;
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public String getFacilityCode() {
        return user.getFacilityCode();
    }
}
