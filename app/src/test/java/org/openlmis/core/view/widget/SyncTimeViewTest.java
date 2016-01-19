package org.openlmis.core.view.widget;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class SyncTimeViewTest {

    protected SyncTimeView syncTimeView;
    protected SharedPreferenceMgr sharedPreferenceMgr;

    @Before
    public void setUp() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_home_page_update, true);
        syncTimeView = new SyncTimeView(LMISTestApp.getContext());
        sharedPreferenceMgr = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SharedPreferenceMgr.class);
    }

    @Test
    public void shouldDisplayGreenIconAndTimeUnitIsMinute() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusMinutes(1).getMillis());
        sharedPreferenceMgr.setRnrLastSyncTime();
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusMinutes(2).getMillis());
        sharedPreferenceMgr.setStockLastSyncTime();

        syncTimeView.onFinishInflate();
        syncTimeView.showLastSyncTime();
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 minutes since last sync"));
        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_green));
    }

    @Test
    public void shouldDisplayGreenIconAndTimeUnitIsHour() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusHours(2).getMillis());
        sharedPreferenceMgr.setRnrLastSyncTime();
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusHours(1).getMillis());
        sharedPreferenceMgr.setStockLastSyncTime();

        syncTimeView.onFinishInflate();
        syncTimeView.showLastSyncTime();
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 hours since last sync"));
        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_green));
    }

    @Test
    public void shouldDisplayYellowIconAndTimeUnitIsDay() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusDays(1).getMillis());
        sharedPreferenceMgr.setRnrLastSyncTime();
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusDays(2).getMillis());
        sharedPreferenceMgr.setStockLastSyncTime();

        syncTimeView.onFinishInflate();
        syncTimeView.showLastSyncTime();
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 days since last sync"));
        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_yellow));
    }

    @Test
    public void shouldDisplayRedIconAndTimeUnitIsDay() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusDays(4).getMillis());
        sharedPreferenceMgr.setRnrLastSyncTime();
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusDays(3).getMillis());
        sharedPreferenceMgr.setStockLastSyncTime();

        syncTimeView.onFinishInflate();
        syncTimeView.showLastSyncTime();
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("3 days since last sync"));
        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_red));
    }
}