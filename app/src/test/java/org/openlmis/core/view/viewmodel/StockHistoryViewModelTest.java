package org.openlmis.core.view.viewmodel;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.utils.DateUtil;

@RunWith(LMISTestRunner.class)
public class StockHistoryViewModelTest {

  public static final String INVENTORY = "INVENTORY";
  StockHistoryViewModel viewModel;
  private StockCard stockCard;

  @Before
  public void setUp() throws Exception {
    setUpViewModel();
  }

  private void setUpViewModel() throws ParseException {
    stockCard = StockCardBuilder.buildStockCard();
    StockMovementItem stockMovementItem1 = new StockMovementItemBuilder()
        .withDocumentNo("1")
        .withMovementDate("2015-11-01")
        .withStockOnHand(300)
        .withQuantity(100)
        .withMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST)
        .withMovementReason(INVENTORY)
        .build();
    stockMovementItem1.setId(1);

    StockMovementItem stockMovementItem2 = new StockMovementItemBuilder()
        .withDocumentNo("2")
        .withMovementDate("2016-1-07")
        .withStockOnHand(300)
        .withQuantity(100)
        .withMovementType(MovementReasonManager.MovementType.ISSUE)
        .withMovementReason(INVENTORY)
        .build();
    stockMovementItem2.setId(2);

    StockMovementItem stockMovementItem3 = new StockMovementItemBuilder()
        .withDocumentNo("3")
        .withMovementDate("2016-12-31")
        .withStockOnHand(300)
        .withQuantity(100)
        .withMovementType(MovementReasonManager.MovementType.RECEIVE)
        .withMovementReason(INVENTORY)
        .build();
    stockMovementItem3.setId(3);

    stockCard.getStockMovementItemsWrapper().add(stockMovementItem3);
    stockCard.getStockMovementItemsWrapper().add(stockMovementItem2);
    stockCard.getStockMovementItemsWrapper().add(stockMovementItem1);
    viewModel = new StockHistoryViewModel(stockCard);
  }

  @Test
  public void shouldConstructStockHistoryViewModelWithMovementItemsInAscendingOderByMovementDate()
      throws Exception {
    assertEquals("1", viewModel.getAllMovementItemViewModelList().get(0).getDocumentNumber());
    assertEquals("2", viewModel.getAllMovementItemViewModelList().get(1).getDocumentNumber());
    assertEquals("3", viewModel.getAllMovementItemViewModelList().get(2).getDocumentNumber());

    StockMovementItem stockMovementItem4 = new StockMovementItemBuilder()
        .withDocumentNo("4")
        .withMovementDate("2016-12-31")
        .withStockOnHand(0)
        .withQuantity(300)
        .withMovementType(MovementReasonManager.MovementType.ISSUE)
        .withMovementReason(INVENTORY)
        .build();
    stockMovementItem4.setId(4);

    stockCard.getStockMovementItemsWrapper().add(0, stockMovementItem4);
    viewModel = new StockHistoryViewModel(stockCard);

    assertEquals("1", viewModel.getAllMovementItemViewModelList().get(0).getDocumentNumber());
    assertEquals("2", viewModel.getAllMovementItemViewModelList().get(1).getDocumentNumber());
    assertEquals("3", viewModel.getAllMovementItemViewModelList().get(2).getDocumentNumber());
    assertEquals("4", viewModel.getAllMovementItemViewModelList().get(3).getDocumentNumber());
  }

  @Test
  public void shouldFilter() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2017-01-06", DateUtil.DB_DATE_FORMAT).getTime());
    viewModel.filter(7);
    assertEquals(1, viewModel.getFilteredMovementItemViewModelList().size());
    assertEquals("3", viewModel.getFilteredMovementItemViewModelList().get(0).getDocumentNumber());

    viewModel.filter(365);
    assertEquals(2, viewModel.getFilteredMovementItemViewModelList().size());
    assertEquals("2", viewModel.getFilteredMovementItemViewModelList().get(0).getDocumentNumber());
    assertEquals("3", viewModel.getFilteredMovementItemViewModelList().get(1).getDocumentNumber());
  }
}