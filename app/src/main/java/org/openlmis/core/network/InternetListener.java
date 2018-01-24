package org.openlmis.core.network;

import lombok.Getter;

@Getter
public class InternetListener {
    private boolean internet;
    private InternetCheck.Callback callback;
    private Exception
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
