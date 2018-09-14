package org.openlmis.core.view.widget;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.SyncErrorsPresenter;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.core.view.widget.SyncTimeView.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class SyncTimeViewTest {

    protected SyncTimeView syncTimeView;
    protected SharedPreferenceMgr sharedPreferenceMgr;
    private SyncErrorsPresenter mockPresenter;

    @Before
    public void setUp() throws Exception {
        mockPresenter = mock(SyncErrorsPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(SyncErrorsPresenter.class).toInstance(mockPresenter);
            }
        });
        syncTimeView = new SyncTimeView(LMISTestApp.getContext());
        sharedPreferenceMgr = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SharedPreferenceMgr.class);
    }

//    @Ignore
//    @Test
//    public void shouldDisplayGreenIconAndTimeUnitIsMinute() throws Exception {
//        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusMinutes(1).getMillis());
//        sharedPreferenceMgr.setRnrLastSyncTime();
//        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusMinutes(2).getMillis());
//        sharedPreferenceMgr.setStockLastSyncTime();
//
//        syncTimeView.showLastSyncTime();
//        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 minute since last sync"));
//        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_green));
//    }
//
//    @Ignore
//    @Test
//    public void shouldDisplayGreenIconAndTimeUnitIsHour() throws Exception {
//        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusHours(2).getMillis());
//        sharedPreferenceMgr.setRnrLastSyncTime();
//        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusHours(1).getMillis());
//        sharedPreferenceMgr.setStockLastSyncTime();
//
//        syncTimeView.showLastSyncTime();
//        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 hour since last sync"));
//        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_green));
//    }
//
//    @Ignore
//    @Test
//    public void shouldDisplayYellowIconAndTimeUnitIsDay() throws Exception {
//        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusDays(1).getMillis());
//        sharedPreferenceMgr.setRnrLastSyncTime();
//        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusDays(2).getMillis());
//        sharedPreferenceMgr.setStockLastSyncTime();
//
//        syncTimeView.showLastSyncTime();
//        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 day since last sync"));
//        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_yellow));
//    }
//
//    @Ignore
//    @Test
//    public void shouldDisplayRedIconAndTimeUnitIsDay() throws Exception {
//        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusDays(4).getMillis());
//        sharedPreferenceMgr.setRnrLastSyncTime();
//        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime().minusDays(3).getMillis());
//        sharedPreferenceMgr.setStockLastSyncTime();
//
//        syncTimeView.showLastSyncTime();
//        assertThat(syncTimeView.txSyncTime.getText().toString(), is("3 days since last sync"));
//        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_red));
//    }
//
//    @Test
//    public void shouldShowErrorMsgWhenNeverSyncSuccessful() throws Exception {
//        LMISTestApp.getInstance().setCurrentTimeMillis(0);
//        sharedPreferenceMgr.setRnrLastSyncTime();
//        LMISTestApp.getInstance().setCurrentTimeMillis(0);
//        sharedPreferenceMgr.setStockLastSyncTime();
//
//        when(mockPresenter.hasStockCardSyncError()).thenReturn(true);
//
//        syncTimeView.showLastSyncTime();
//        assertThat(syncTimeView.txSyncTime.getText().toString(), is("Initial sync failed"));
//        assertNull(syncTimeView.ivSyncTimeIcon.getDrawable());
//
//        when(mockPresenter.hasRnrSyncError()).thenReturn(true);
//        syncTimeView.showLastSyncTime();
//        assertThat(syncTimeView.txSyncTime.getText().toString(), is("Initial sync failed"));
//        assertNull(syncTimeView.ivSyncTimeIcon.getDrawable());
//    }
//
//    @Test
//    public void shouldHideProgressBarAndShowSyncTimeIconWhenShowLastSyncTime() throws Exception {
//        syncTimeView.showLastSyncTime();
//        assertThat(syncTimeView.progressBar.getVisibility(),is(GONE));
//        assertThat(syncTimeView.ivSyncTimeIcon.getVisibility(),is(VISIBLE));
//    }
}