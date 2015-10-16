package org.openlmis.core;


public class LMISTestApp extends LMISApp {

    private boolean networkAvailable;

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
}
