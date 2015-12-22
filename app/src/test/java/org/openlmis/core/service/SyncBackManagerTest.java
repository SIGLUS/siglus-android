package org.openlmis.core.service;

import android.content.Context;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SyncBackManagerTest {
    private SharedPreferenceMgr sharedPreferenceMgr;
    private LMISRestApi lmisRestApi;
    private RnrFormRepository rnrFormRepository;
    private SyncBackManager syncBackManager;

    @Before
    public void setUp() throws Exception {
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        lmisRestApi = mock(LMISRestApi.class);
        rnrFormRepository = mock(RnrFormRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        syncBackManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncBackManager.class);

        syncBackManager.lmisRestApi = lmisRestApi;

        UserInfoMgr.getInstance().setUser(new User());
    }

    @Test
    public void shouldSyncRequisitionDataSuccess() throws LMISException, SQLException {
        when(sharedPreferenceMgr.getPreference()).thenReturn(LMISTestApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE));
        List<RnRForm> data = new ArrayList<>();
        data.add(new RnRForm());
        data.add(new RnRForm());

        SyncDownRequisitionsResponse syncDownRequisitionsResponse = new SyncDownRequisitionsResponse();
        syncDownRequisitionsResponse.setRequisitions(data);
        when(lmisRestApi.fetchRequisitions(anyString())).thenReturn(syncDownRequisitionsResponse);
        syncBackManager.fetchAndSaveRequisitionData();
        verify(rnrFormRepository, times(2)).createFormAndItems(any(RnRForm.class));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        }
    }
}

