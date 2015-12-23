package org.openlmis.core;


import org.apache.commons.lang.NotImplementedException;
import org.openlmis.core.exceptions.LMISException;

public class LMISTestApp extends LMISApp {

    private boolean networkAvailable;
    private long currentTimeMillis;

    @Override
    protected void setupFabric() {
    }

    public void setNetworkConnection(boolean networkAvailable) {
        this.networkAvailable = networkAvailable;
    }

    public void setFeatureToggle(boolean featureToggle) {
        throw new NotImplementedException("No need to call this, all toggles are turned on for test");
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
        return true;
    }

    @Override
    public void logErrorOnFabric(LMISException exception) {
        //do nothing
    }
}
