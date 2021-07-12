package org.openlmis.core.service.mocks;

import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.InternetListener;

public class InternetCheckMockForNetworkChangeReceiver extends InternetCheck {

  private final boolean withInternet;

  public InternetCheckMockForNetworkChangeReceiver(final boolean withInternet) {
    this.withInternet = withInternet;
  }

  @Override
  public InternetListener doInBackground(Callback... callbacks) {
    return new InternetListener(withInternet, callbacks[0], withInternet ? null : new Exception());
  }

  @Override
  protected void onPostExecute(InternetListener internetListener) {
    internetListener.launchCallback();
  }
}
