package org.openlmis.core;


import org.openlmis.core.exceptions.LMISException;

import java.util.HashMap;

public class LMISTestApp extends LMISApp {

    private boolean networkAvailable;
    private long currentTimeMillis;
    private HashMap<Integer, Boolean> featureToggles = new HashMap<>();
    private static LMISTestApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    protected void setupFabric() {
    }

    public static LMISTestApp getInstance() {
        return instance;
    }

    public void setNetworkConnection(boolean networkAvailable) {
        this.networkAvailable = networkAvailable;
    }

    public void setFeatureToggle(int id, boolean featureToggle) {
        featureToggles.put(id, featureToggle);
    }

    @Override
    public boolean isConnectionAvailable() {
        return networkAvailable;
    }

    public void setCurrentTimeMillis(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    @Override
    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    @Override
    public boolean getFeatureToggleFor(int id) {
        return featureToggles.get(id) == null ? false : featureToggles.get(id);
    }

    @Override
    public void logErrorOnFabric(LMISException exception) {
        //do nothing
    }

    @Override
    protected void setupGoogleAnalytics() {
    }
}
