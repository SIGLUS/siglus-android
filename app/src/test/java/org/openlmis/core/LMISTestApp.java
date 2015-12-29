package org.openlmis.core;


import org.openlmis.core.exceptions.LMISException;

import java.util.HashMap;

public class LMISTestApp extends LMISApp {

    private boolean networkAvailable;
    private long currentTimeMillis;
    private HashMap<Integer, Boolean> featureToggles = new HashMap<>();

    @Override
    protected void setupFabric() {
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
        return featureToggles.get(id);
    }

    @Override
    public void logErrorOnFabric(LMISException exception) {
        //do nothing
    }
}
