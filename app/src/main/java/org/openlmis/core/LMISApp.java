package org.openlmis.core;

import android.app.Application;

import roboguice.RoboGuice;

public class LMISApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RoboGuice.getInjector(this).injectMembersWithoutViews(this);
    }
}
