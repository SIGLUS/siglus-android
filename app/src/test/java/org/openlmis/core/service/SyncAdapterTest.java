package org.openlmis.core.service;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SyncAdapterTest {
    private SyncAdapter syncAdapter;
    private SyncManager mockSyncManager;
    private SharedPreferenceMgr sharedPreferenceMgr;

    @Before
    public void setUp() throws Exception {
        mockSyncManager = mock(SyncManager.class);
        sharedPreferenceMgr = new SharedPreferenceMgr(RuntimeEnvironment.application);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        syncAdapter = new SyncAdapter(RuntimeEnvironment.application, true);
        syncAdapter.sharedPreferenceMgr = this.sharedPreferenceMgr;
        sharedPreferenceMgr.getPreference().edit().clear();
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);
    }

    @Test
    public void shouldRecordCorrespondingLastSyncTime() throws Exception {
        UserInfoMgr.getInstance().setUser(new User());
        when(mockSyncManager.syncRnr()).thenReturn(true);
        when(mockSyncManager.syncStockCards()).thenReturn(true);

        syncAdapter.onPerformSync(null, null, null, null, null);

        long lastRnrFormSyncedTimestamp = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_RNR_FORM, 0);
        long lastStockCardSyncedTimestamp = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_STOCKCARD, 0);

        Date rnrFormDate = new Date(lastRnrFormSyncedTimestamp);
        Date stockCardDate = new Date(lastStockCardSyncedTimestamp);

        Date expectDate = new Date();
        assertThat(rnrFormDate.getDay(), is(expectDate.getDay()));
        assertThat(rnrFormDate.getHours(), is(expectDate.getHours()));
        assertThat(stockCardDate.getDay(), is(expectDate.getDay()));
        assertThat(stockCardDate.getHours(), is(expectDate.getHours()));
    }

    @Test
    public void shouldNotRecordLastSyncTime() throws Exception {
        UserInfoMgr.getInstance().setUser(new User());
        when(mockSyncManager.syncRnr()).thenReturn(false);
        when(mockSyncManager.syncStockCards()).thenReturn(false);

        syncAdapter.onPerformSync(null, null, null, null, null);

        long lastRnrFormSyncedTimestamp = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_RNR_FORM, 0);
        long lastStockCardSyncedTimestamp = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_STOCKCARD, 0);

        assertEquals(0, lastRnrFormSyncedTimestamp);
        assertEquals(0, lastStockCardSyncedTimestamp);
    }


    @Test
    public void shouldOnlyRecordRnrFormLastSyncTime() throws Exception {
        UserInfoMgr.getInstance().setUser(new User());
        when(mockSyncManager.syncRnr()).thenReturn(true);
        when(mockSyncManager.syncStockCards()).thenReturn(false);

        syncAdapter.onPerformSync(null, null, null, null, null);

        long lastRnrFormSyncedTimestamp = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_RNR_FORM, 0);
        long lastStockCardSyncedTimestamp = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_STOCKCARD, 0);

        Date rnrFormDate = new Date(lastRnrFormSyncedTimestamp);

        Date expectDate = new Date();
        assertThat(rnrFormDate.getDay(), is(expectDate.getDay()));
        assertThat(rnrFormDate.getHours(), is(expectDate.getHours()));
        assertEquals(0,lastStockCardSyncedTimestamp);
    }

    @Test
    public void shouldOnlyRecordStockCardLastSyncTime() throws Exception {
        UserInfoMgr.getInstance().setUser(new User());
        when(mockSyncManager.syncRnr()).thenReturn(false);
        when(mockSyncManager.syncStockCards()).thenReturn(true);

        syncAdapter.onPerformSync(null, null, null, null, null);

        long lastRnrFormSyncedTimestamp = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_RNR_FORM, 0);
        long lastStockCardSyncedTimestamp = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_STOCKCARD, 0);

        Date stockCardTime = new Date(lastStockCardSyncedTimestamp);

        Date expectDate = new Date();
        assertThat(stockCardTime.getDay(), is(expectDate.getDay()));
        assertThat(stockCardTime.getHours(), is(expectDate.getHours()));
        assertEquals(0,lastRnrFormSyncedTimestamp);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(SyncManager.class).toInstance(mockSyncManager);
        }
    }
}