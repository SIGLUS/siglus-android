package org.openlmis.core;


public class LMISTestApp extends LMISApp {

    private boolean networkAvailable;
    private long currentTimeMillis;
    private boolean featureToggle;

    @Override
    protected void setupFabric() {
    }

    public void setNetworkConnection(boolean networkAvailable) {
        this.networkAvailable = networkAvailable;
    }

    public void setFeatureToggle(boolean featureToggle) {
        this.featureToggle = featureToggle;
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
        return featureToggle;
    }
}
