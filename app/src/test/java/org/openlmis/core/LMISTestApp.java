package org.openlmis.core;


import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.network.LMISRestApi;

import java.util.HashMap;

public class LMISTestApp extends LMISApp {

    private boolean networkAvailable;
    private long currentTimeMillis;
    private HashMap<Integer, Boolean> featureToggles = new HashMap<>();
    private static LMISTestApp instance;
    private LMISRestApi restApi;

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
    public LMISRestApi getRestApi() {
        return restApi;
    }

    public void setRestApi(LMISRestApi restApi) {
        this.restApi = restApi;
    }

    @Override
    public void logErrorOnFabric(LMISException exception) {
        //do nothing
    }

    @Override
    protected void setupGoogleAnalytics() {
    }

    @Override
    public void trackEvent(TrackerCategories category, TrackerActions action) {
    }

    @Override
    public void trackScreen(ScreenName screenName) {
    }

    @Override
    public boolean isQAEnabled() {
        return false;
    }


}
