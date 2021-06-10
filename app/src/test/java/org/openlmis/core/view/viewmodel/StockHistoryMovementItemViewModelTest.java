package org.openlmis.core.view.viewmodel;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.StockMovementItemBuilder;

@RunWith(LMISTestRunner.class)
public class StockHistoryMovementItemViewModelTest {

  StockHistoryMovementItemViewModel viewModel;

  @Before
  public void setUp() throws Exception {
    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withDocumentNo("123")
        .withMovementDate("2016-11-01")
        .withStockOnHand(300)
        .withQuantity(100)
        .withMovementType(MovementReasonManager.MovementType.ISSUE)
        .withMovementReason("INVENTORY")
        .build();
    viewModel = new StockHistoryMovementItemViewModel(stockMovementItem);
  }

  @Test
  public void shouldGetAllColumnValues() throws Exception {
    assertEquals("123", viewModel.getDocumentNumber());
    assertEquals("01 Nov 2016", viewModel.getMovementDate());
    assertEquals("-", viewModel.getEntryAmount());
    assertEquals("-", viewModel.getPositiveAdjustmentAmount());
    assertEquals("-", viewModel.getNegativeAdjustmentAmount());
    assertEquals("100", viewModel.getIssueAmount());
    assertEquals("300", viewModel.getStockExistence());
  }
}