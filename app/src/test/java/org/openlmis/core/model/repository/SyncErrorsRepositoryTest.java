package org.openlmis.core.model.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class SyncErrorsRepositoryTest extends LMISRepositoryUnitTest {

  SyncErrorsRepository syncErrorsRepository;

  @Before
  public void setUp() throws Exception {
    syncErrorsRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(SyncErrorsRepository.class);
  }

  @Test
  public void createOrUpdate_shouldSaveSyncErrorWhenNoExistingSyncError() {
    // given
    long syncObjectId = 1L;
    SyncError expectSyncError = new SyncError("errorMessage", SyncType.RNR_FORM, syncObjectId);
    // when
    syncErrorsRepository.createOrUpdate(expectSyncError);
    // then
    SyncError actualSyncError = syncErrorsRepository.getBySyncTypeAndObjectId(SyncType.RNR_FORM,
            syncObjectId)
        .get(0);
    assertEquals(expectSyncError.getErrorMessage(), actualSyncError.getErrorMessage());
  }

  @Test
  public void createOrUpdate_shouldUpdateSyncErrorWhenHasExistingSyncError() {
    // given
    long syncObjectId = 1L;
    SyncError existingSyncError = new SyncError("errorMessage", SyncType.RNR_FORM, syncObjectId);
    String newErrorMessage = "newErrorMessage";
    SyncError newSyncError = new SyncError(newErrorMessage, SyncType.RNR_FORM, syncObjectId);
    // when
    syncErrorsRepository.createOrUpdate(existingSyncError);
    syncErrorsRepository.createOrUpdate(newSyncError);
    // then
    List<SyncError> syncErrors = syncErrorsRepository.getBySyncTypeAndObjectId(
        SyncType.RNR_FORM, syncObjectId
    );

    assertEquals(1, syncErrors.size());
    SyncError actualSyncError = syncErrors.get(0);
    assertEquals(newErrorMessage, actualSyncError.getErrorMessage());
  }

  @Test
  public void shouldDeleteSyncError() {
    saveRnRFormError();

    Integer deletedSize = syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RNR_FORM, 1l);

    assertEquals(1, deletedSize.intValue());
  }

  @Test
  public void shouldGetRnrHasSyncErrorIsFalse() throws Exception {
    boolean hasRnrSyncError = syncErrorsRepository.hasSyncErrorOf(SyncType.RNR_FORM);
    assertFalse(hasRnrSyncError);
  }

  @Test
  public void shouldGetRnrHasSyncErrorIsTrueWhenHaveSyncFailed() throws Exception {
    saveRnRFormError();
    boolean hasRnrSyncError = syncErrorsRepository.hasSyncErrorOf(SyncType.RNR_FORM);
    assertTrue(hasRnrSyncError);
  }

  private void saveRnRFormError() {
    SyncError syncError1 = new SyncError("errorMessage1", SyncType.RNR_FORM, 1l);
    SyncError syncError2 = new SyncError("errorMessage2", SyncType.RNR_FORM, 1l);
    SyncError syncError3 = new SyncError("errorMessage3", SyncType.RNR_FORM, 2l);
    syncErrorsRepository.createOrUpdate(syncError1);
    syncErrorsRepository.createOrUpdate(syncError2);
    syncErrorsRepository.createOrUpdate(syncError3);
  }
}