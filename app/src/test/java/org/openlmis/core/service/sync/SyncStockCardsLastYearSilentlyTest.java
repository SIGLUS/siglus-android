package org.openlmis.core.service.sync;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.utils.Constants;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class SyncStockCardsLastYearSilentlyTest {

  private final LMISRestApi mockRestApi = mock(LMISRestApi.class);
  private SyncStockCardsLastYearSilently syncStockCardsLastYearSilently;

  @Before
  public void setUp() {
    syncStockCardsLastYearSilently = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(SyncStockCardsLastYearSilently.class);
  }

  @Test
  public void shouldSyncLastYearStockCards() throws LMISException {
    // given
    LMISTestApp.getInstance().setRestApi(mockRestApi);
    Mockito.reset(mockRestApi);
    final TestSubscriber<List<StockCard>> listTestSubscriber = new TestSubscriber<>();
    when(mockRestApi.fetchStockMovementData(any(), any())).thenReturn(new StockCardsLocalResponse());
    // when
    syncStockCardsLastYearSilently.performSync().subscribe(listTestSubscriber);
    listTestSubscriber.awaitTerminalEvent();

    // then
    verify(mockRestApi, times(Constants.STOCK_CARD_MAX_SYNC_MONTH)).fetchStockMovementData(any(), any());
  }
}
