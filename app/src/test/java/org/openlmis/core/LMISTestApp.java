package org.openlmis.core;


public class LMISTestApp extends LMISApp {

    private boolean networkAvailable;
    private long currentTimeMillis;

    @Override
    protected void setupFabric() {
    }

    public void setNetworkConnection(boolean networkAvailable) {
        this.networkAvailable = networkAvailable;
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
}
