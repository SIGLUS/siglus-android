package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SyncErrorsPresenterTest{

    protected SyncErrorsPresenter presenter;
    protected SyncErrorsRepository errorsRepository;

    @Before
    public void setup() throws Exception {
        errorsRepository = mock(SyncErrorsRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncErrorsPresenter.class);
    }

    @Test
    public void shouldGetSyncErrorsWhenHaveSyncFailed() throws Exception {
        when(errorsRepository.hasSyncErrorOf(SyncType.RnRForm)).thenReturn(true);
        when(errorsRepository.hasSyncErrorOf(SyncType.StockCards)).thenReturn(false);
        boolean hasRnrSyncError = presenter.hasRnrSyncError(SyncType.RnRForm);
        boolean hasStockCardsSyncError = presenter.hasRnrSyncError(SyncType.StockCards);
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