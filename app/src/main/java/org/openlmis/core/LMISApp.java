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

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.network.NetworkConnectionManager;

import io.fabric.sdk.android.Fabric;
import roboguice.RoboGuice;

public class LMISApp extends Application {

    private static LMISApp instance;

    public static long lastOperateTime = 0L;

    @Override
    public void onCreate() {
        super.onCreate();

        RoboGuice.getInjector(this).injectMembersWithoutViews(this);
        setupFabric();

        instance = this;
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
}
