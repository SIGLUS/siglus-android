package org.openlmis.core.training;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.service.SyncUpManager;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class TrainingSyncAdapterTest {
    private TrainingSyncAdapter trainingSyncAdapter;
    private SyncUpManager mockSyncUpManager;
    private SharedPreferenceMgr sharedPreferenceMgr;

    @Before
    public void setUp() throws Exception {
        mockSyncUpManager = mock(SyncUpManager.class);
        sharedPreferenceMgr = new SharedPreferenceMgr(RuntimeEnvironment.application);
        trainingSyncAdapter = new TrainingSyncAdapter();
        trainingSyncAdapter.sharedPreferenceMgr = sharedPreferenceMgr;
        trainingSyncAdapter.syncUpManager = mockSyncUpManager;
        sharedPreferenceMgr.getPreference().edit().clear();
        UserInfoMgr.getInstance().setUser(new User());
        LMISTestApp.getInstance().setCurrentTimeMillis(new Date().getTime());
    }

    // TODO:
//    @Ignore
    @Test
    public void shouldRequestTrainingSyncWhenTrainingFeatureIsOn() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_training, true);
        trainingSyncAdapter.onPerformSync();

        verify(mockSyncUpManager).fakeSyncRnr();
        verify(mockSyncUpManager).fakeSyncStockCards();
        verify(mockSyncUpManager).fakeSyncUpCmms();

        when(mockSyncUpManager.fakeSyncRnr()).thenReturn(true);
        when(mockSyncUpManager.fakeSyncStockCards()).thenReturn(true);
        trainingSyncAdapter.onPerformSync();
        long lastRnrFormSyncedTimestamp = sharedPreferenceMgr.getRnrLastSyncTime();
        long lastStockCardSyncedTimestamp = sharedPreferenceMgr.getStockLastSyncTime();
        DateTime rnrFormDate = new DateTime(lastRnrFormSyncedTimestamp);
        DateTime stockCardDate = new DateTime(lastStockCardSyncedTimestamp);

        DateTime expectDate = new DateTime();
        assertThat(rnrFormDate.getDayOfMonth(), is(expectDate.getDayOfMonth()));
        assertThat(rnrFormDate.getHourOfDay(), is(expectDate.getHourOfDay()));
        assertThat(stockCardDate.getDayOfMonth(), is(expectDate.getDayOfMonth()));
        assertThat(stockCardDate.getHourOfDay(), is(expectDate.getHourOfDay()));
    }
}