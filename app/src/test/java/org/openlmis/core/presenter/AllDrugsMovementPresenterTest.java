package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.StockHistoryViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class AllDrugsMovementPresenterTest {

  AllDrugsMovementPresenter mPresenter;

  StockRepository mockStockRepository;

  @Before
  public void setUp() throws Exception {
    mockStockRepository = mock(StockRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    mPresenter = RoboGuice.getInjector(LMISApp.getContext())
        .getInstance(AllDrugsMovementPresenter.class);
  }

  @Test
  public void shouldLoadAllStockCardsWithMovement() throws Exception {
    StockCard stockcard1 = StockCardBuilder.buildStockCard();
    StockMovementItem stockMovementItem = new StockMovementItem(stockcard1);
    stockMovementItem.setMovementType(MovementType.PHYSICAL_INVENTORY);
    stockMovementItem.setReason("INVENTORY");
    stockcard1.getStockMovementItemsWrapper().add(stockMovementItem);

    StockCard stockcard2 = StockCardBuilder.buildStockCard();
    List<StockCard> stockCards = Arrays.asList(stockcard1, stockcard2);
    when(mockStockRepository.list()).thenReturn(stockCards);
    mPresenter.loadAllMovementHistory();

    assertEquals(2, mPresenter.viewModelList.size());
  }

  @Test
  public void shouldFilterViewModels() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2016-09-01", DateUtil.DB_DATE_FORMAT).getTime());
    StockCard stockcard1 = StockCardBuilder.buildStockCard();
    StockMovementItem stockMovementItem1 = new StockMovementItem(stockcard1);
    stockMovementItem1.setMovementDate(DateUtil.parseString("2016-08-26", DateUtil.DB_DATE_FORMAT));
    stockMovementItem1.setMovementType(MovementType.PHYSICAL_INVENTORY);
    stockMovementItem1.setReason("INVENTORY");
    stockcard1.getStockMovementItemsWrapper().add(stockMovementItem1);

    mPresenter.viewModelList.add(new StockHistoryViewModel(stockcard1));
    mPresenter.filterViewModels(7);
    assertEquals(1, mPresenter.filteredViewModelList.size());
  }

  class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(StockRepository.class).toInstance(mockStockRepository);
    }
  }
}
