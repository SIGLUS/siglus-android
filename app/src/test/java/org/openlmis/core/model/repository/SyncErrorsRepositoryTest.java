package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class SyncErrorsRepositoryTest extends LMISRepositoryUnitTest {

    SyncErrorsRepository syncErrorsRepository ;

    @Before
    public void setUp() throws Exception {
        syncErrorsRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncErrorsRepository.class);
    }

    @Test
    public void shouldSaveSyncError() throws Exception {
        SyncError expectSyncError = new SyncError("errorMessage", SyncType.RnRForm,1l);
        syncErrorsRepository.save(expectSyncError);

        SyncError actualSyncError = syncErrorsRepository.getBySyncTypeAndObjectId(SyncType.RnRForm,1l).get(0);

        assertThat(expectSyncError.getErrorMessage(), is(actualSyncError.getErrorMessage()));
    }

    @Test
    public void shouldDeleteSyncError() throws Exception {
        SyncError syncError1 = new SyncError("errorMessage1", SyncType.RnRForm,1l);
        SyncError syncError2 = new SyncError("errorMessage2", SyncType.RnRForm,1l);
        SyncError syncError3 = new SyncError("errorMessage3", SyncType.RnRForm,2l);
        syncErrorsRepository.save(syncError1);
        syncErrorsRepository.save(syncError2);
        syncErrorsRepository.save(syncError3);

        Integer deletedSize = syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RnRForm, 1l);

        assertThat(deletedSize, is(2));
    }


}