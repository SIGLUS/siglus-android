package org.openlmis.core.view.activity.mocks;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.InternetListener;
import org.openlmis.core.view.fragment.builders.WarningDialogFragmentBuilder;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class InternetCheckMockForHomeActivity extends InternetCheck {
    private boolean withInternet;
    private WarningDialogFragmentBuilder warningDialogFragmentBuilder;

    public InternetCheckMockForHomeActivity(final boolean withInternet, WarningDialogFragmentBuilder warningDialogFragmentBuilder) {
        this.withInternet = withInternet;
        this.warningDialogFragmentBuilder = warningDialogFragmentBuilder;
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
            verify(warningDialogFragmentBuilder, times(1)).build((WarningDialogFragment.DialogDelegate) anyObject(), (int) anyObject(), (int) anyObject(), (int) anyObject());
        } else {
            String toastMessage = ShadowToast.getTextOfLatestToast();
            assertThat(toastMessage, is(LMISApp.getInstance().getString(R.string.message_wipe_no_connection)));
        }
    }


}