package org.openlmis.core.service.mocks;

import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.InternetListener;
import org.openlmis.core.service.SyncService;

import static org.mockito.Mockito.verify;

public class InternetCheckMockForNetworkChangeReceiver extends InternetCheck {
    private boolean withInternet;
    private SyncService syncService;

    public InternetCheckMockForNetworkChangeReceiver(final boolean withInternet, final SyncService syncService) {
        this.withInternet = withInternet;
        this.syncService = syncService;
    }

    @Override
    public InternetListener doInBackground(Callback... callbacks) {
        if (withInternet) {
            return new InternetListener(withInternet, callbacks[0], null);
        } else {
            return new InternetListener(withInternet, callbacks[0], new Exception());
        }
    }

    @Override
    protected void onPostExecute(InternetListener internetListener) {
        internetListener.launchCallback();
        if (internetListener.isInternet()) {
            verify(syncService).kickOff();
        } else {
            verify(syncService).shutDown();
        }
    }
}
