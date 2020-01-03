package org.openlmis.core.view.widget;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.robolectric.RuntimeEnvironment;


import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.core.view.widget.SyncTimeView.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class SyncTimeViewTest {

    protected SyncTimeView syncTimeView;
    protected SharedPreferenceMgr sharedPreferenceMgr;
    private SyncErrorsRepository syncErrorsRepository;
    private DateTime nowDateTime;

    @Before
    public void setUp() throws Exception {
        syncErrorsRepository = mock(SyncErrorsRepository.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyMode());
        syncTimeView = new SyncTimeView(LMISTestApp.getContext());
        sharedPreferenceMgr = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SharedPreferenceMgr.class);
        nowDateTime = new DateTime();
    }

    @Test
    public void shouldDisplayGreenIconAndTimeUnitIsMinute() throws Exception {
        // Given
        when(sharedPreferenceMgr.getRnrLastSyncTime()).thenReturn(nowDateTime.minusMinutes(1).getMillis());
        when(sharedPreferenceMgr.getStockLastSyncTime()).thenReturn(nowDateTime.minusMinutes(2).getMillis());
        // When
        syncTimeView.showLastSyncTime();
        // Then
        assertThat(sharedPreferenceMgr.getStockLastSyncTime(), is(nowDateTime.minusMinutes(2).getMillis()));
        assertThat(sharedPreferenceMgr.getRnrLastSyncTime(), is(nowDateTime.minusMinutes(1).getMillis()));
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 minute since last sync"));
        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_green));
    }

    @Test
    public void shouldDisplayGreenIconAndTimeUnitIsHour() throws Exception {
        // Given
        when(sharedPreferenceMgr.getRnrLastSyncTime()).thenReturn(nowDateTime.minusHours(2).getMillis());
        when(sharedPreferenceMgr.getStockLastSyncTime()).thenReturn(nowDateTime.minusHours(1).getMillis());
        // When
        syncTimeView.showLastSyncTime();
        // Then

        assertThat(sharedPreferenceMgr.getStockLastSyncTime(), is(nowDateTime.minusHours(1).getMillis()));
        assertThat(sharedPreferenceMgr.getRnrLastSyncTime(), is(nowDateTime.minusHours(2).getMillis()));
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 hour since last sync"));
        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_green));
    }

    @Test
    public void shouldDisplayYellowIconAndTimeUnitIsDay() throws Exception {
        // Given
        when(sharedPreferenceMgr.getRnrLastSyncTime()).thenReturn(nowDateTime.minusDays(1).getMillis());
        when(sharedPreferenceMgr.getStockLastSyncTime()).thenReturn(nowDateTime.minusDays(2).getMillis());
        // When
        syncTimeView.showLastSyncTime();
        // Then
        assertThat(sharedPreferenceMgr.getStockLastSyncTime(), is(nowDateTime.minusDays(2).getMillis()));
        assertThat(sharedPreferenceMgr.getRnrLastSyncTime(), is(nowDateTime.minusDays(1).getMillis()));
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("1 day since last sync"));
        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_yellow));
    }

    @Test
    public void shouldDisplayRedIconAndTimeUnitIsDay() throws Exception {
        // Given
        when(sharedPreferenceMgr.getRnrLastSyncTime()).thenReturn(nowDateTime.minusDays(4).getMillis());
        when(sharedPreferenceMgr.getStockLastSyncTime()).thenReturn(nowDateTime.minusDays(3).getMillis());
        // When
        syncTimeView.showLastSyncTime();
        // Then
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("3 days since last sync"));
        assertThat(shadowOf(syncTimeView.ivSyncTimeIcon.getDrawable()).getCreatedFromResId(), is(R.drawable.icon_circle_red));
    }

    @Test
    public void shouldShowErrorMsgWhenNeverSyncSuccessful() throws Exception {
        //Given
        when(sharedPreferenceMgr.getRnrLastSyncTime()).thenReturn(0l);
        when(sharedPreferenceMgr.getStockLastSyncTime()).thenReturn(0l);
        when(syncErrorsRepository.hasSyncErrorOf(anyObject())).thenReturn(true);

        //When
        syncTimeView.showLastSyncTime();
        //Then
        assertThat(syncTimeView.txSyncTime.getText().toString(), is("Initial sync failed"));
        assertNull(syncTimeView.ivSyncTimeIcon.getDrawable());
    }

    @Test
    public void shouldHideProgressBarAndShowSyncTimeIconWhenShowLastSyncTime() throws Exception {
        syncTimeView.showLastSyncTime();
        assertThat(syncTimeView.progressBar.getVisibility(), is(GONE));
        assertThat(syncTimeView.ivSyncTimeIcon.getVisibility(), is(VISIBLE));
    }

    private class MyMode implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(SyncErrorsRepository.class).toInstance(syncErrorsRepository);
            binder.bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        }
    }
}