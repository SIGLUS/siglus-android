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
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.analytics.GoogleAnalytics;

import net.danlew.android.joda.JodaTimeAndroid;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.network.NetworkConnectionManager;
import org.openlmis.core.service.AnalyticsTrackers;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import roboguice.RoboGuice;

public class LMISApp extends Application {

    private static LMISApp instance;

    public static long lastOperateTime = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);

        RoboGuice.getInjector(this).injectMembersWithoutViews(this);
        setupFabric();
        setupGoogleAnalytics();

        instance = this;
    }

    protected void setupGoogleAnalytics() {
        AnalyticsTrackers.initialize(this);

        if (!getGoogleAnalyticsEnvs().contains(BuildConfig.FLAVOR)) {
            GoogleAnalytics.getInstance(this).setDryRun(true);
        }
    }

    @NonNull
    private ArrayList<String> getGoogleAnalyticsEnvs() {
        ArrayList<String> googleAnalyticsEnvs = new ArrayList<>();
        googleAnalyticsEnvs.add("prd");
        googleAnalyticsEnvs.add("qa");
        googleAnalyticsEnvs.add("local");
        return googleAnalyticsEnvs;
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
