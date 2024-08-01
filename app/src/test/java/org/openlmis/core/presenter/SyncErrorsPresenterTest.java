package org.openlmis.core.presenter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class SyncErrorsPresenterTest {

  protected SyncErrorsPresenter presenter;
  protected SyncErrorsRepository errorsRepository;

  @Before
  public void setup() throws Exception {
    errorsRepository = mock(SyncErrorsRepository.class);
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new MyTestModule());

    presenter = RoboGuice.getInjector(ApplicationProvider.getApplicationContext())
        .getInstance(SyncErrorsPresenter.class);
  }

  @Test
  public void shouldGetSyncErrorsWhenHaveSyncFailed() throws Exception {
    when(errorsRepository.hasSyncErrorOf(SyncType.RNR_FORM)).thenReturn(true);
    when(errorsRepository.hasSyncErrorOf(SyncType.STOCK_CARDS)).thenReturn(false);
    boolean hasRnrSyncError = presenter.hasRnrSyncError();
    boolean hasStockCardsSyncError = presenter.hasStockCardSyncError();
    assertTrue(hasRnrSyncError);
    assertFalse(hasStockCardsSyncError);
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(SyncErrorsRepository.class).toInstance(errorsRepository);
    }
  }
}