package org.openlmis.core.network;

import lombok.Getter;

@Getter
public class InternetListener {

  private final boolean internet;
  private final InternetCheck.Callback callback;
  private final Exception
      exception;

  public InternetListener(boolean internet, InternetCheck.Callback callback, Exception exception) {
    this.internet = internet;
    this.callback = callback;
    this.exception = exception;
  }

  public void launchCallback() {
    callback.launchResponse(internet);
  }
}
