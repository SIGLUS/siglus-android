package org.openlmis.core.service.sync;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.network.LMISRestApi;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MalariaProgramSyncTaskTest {

    private LMISRestApi restApi = mock(LMISRestApi.class);
    private MalariaProgramRepository malariaProgramRepository = mock(MalariaProgramRepository.class);
    private MalariaProgramSyncTask syncTask;
    private List<MalariaProgram> pendingForSync;

    @Before
    public void setUp() throws Exception {
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        syncTask = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MalariaProgramSyncTask.class);
        pendingForSync = mock(List.class);
        when(malariaProgramRepository.getPendingForSync()).thenReturn(pendingForSync);
    }

    @Test
    public void shouldImplementsSyncronizableTask() throws Exception {
        assertThat(syncTask, is(instanceOf(SyncronizableTask.class)));
    }

    @Test
    public void shouldSyncPendingMalariaPrograms() throws Exception {
        InOrder inOrder = inOrder(malariaProgramRepository, restApi);
        syncTask.sync();
        inOrder.verify(restApi).syncUpMalariaPrograms(pendingForSync);
        inOrder.verify(malariaProgramRepository).bulkUpdateAsSynced(pendingForSync);
    }

    @Test
    public void shouldReportToFabricWhenRestError() throws Exception {
        LMISException lmisException = mock(LMISException.class);
        doThrow(lmisException).when(restApi).syncUpMalariaPrograms(pendingForSync);
        syncTask.sync();
        verify(lmisException).reportToFabric();
    }

    @Test
    public void shouldReportErrorToFabricWhenAccessingPendingForSync() throws Exception {
        LMISException lmisException = mock(LMISException.class);
        doThrow(lmisException).when(malariaProgramRepository).getPendingForSync();
        syncTask.sync();
        verify(lmisException).reportToFabric();
    }

    @Test
    public void shouldReportErrorToFabricWhenUpdatingSyncedPrograms() throws Exception {
        LMISException lmisException = mock(LMISException.class);
        doThrow(lmisException).when(malariaProgramRepository).bulkUpdateAsSynced((List<MalariaProgram>) anyObject());
        syncTask.sync();
        verify(lmisException).reportToFabric();
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(LMISRestApi.class).toInstance(restApi);
            bind(MalariaProgramRepository.class).toInstance(malariaProgramRepository);
        }
    }
}