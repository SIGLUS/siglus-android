package org.openlmis.core;

import java.util.HashMap;
import org.openlmis.core.network.LMISRestApi;

public class LMISTestApp extends LMISApp {

  private long currentTimeMillis;
  private final HashMap<Integer, Boolean> featureToggles = new HashMap<>();
  private static LMISTestApp instance;
  private LMISRestApi restApi;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
  }

  public static LMISTestApp getInstance() {
    return instance;
  }


  public void setFeatureToggle(int id, boolean featureToggle) {
    featureToggles.put(id, featureToggle);
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
    return featureToggles.get(id) != null && featureToggles.get(id);
  }

  @Override
  public LMISRestApi getRestApi() {
    return restApi;
  }

  public void setRestApi(LMISRestApi restApi) {
    this.restApi = restApi;
  }
}
