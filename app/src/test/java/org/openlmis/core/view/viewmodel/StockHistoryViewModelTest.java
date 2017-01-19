package org.openlmis.core.view.viewmodel;

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

import java.text.ParseException;

import static org.junit.Assert.assertEquals;

@RunWith(LMISTestRunner.class)
public class StockHistoryViewModelTest {

    StockHistoryViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        setUpViewModel();
    }

    private void setUpViewModel() throws ParseException {
        StockCard stockCard = StockCardBuilder.buildStockCard();
        StockMovementItem stockMovementItem1 = new StockMovementItemBuilder()
                .withDocumentNo("1")
                .withMovementDate("2015-11-01")
                .withStockOnHand(300)
                .withQuantity(100)
                .withMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST)
                .build();
        stockCard.getStockMovementItemsWrapper().add(stockMovementItem1);

        StockMovementItem stockMovementItem2 = new StockMovementItemBuilder()
                .withDocumentNo("2")
                .withMovementDate("2016-1-07")
                .withStockOnHand(300)
                .withQuantity(100)
                .withMovementType(MovementReasonManager.MovementType.ISSUE)
                .build();
        stockCard.getStockMovementItemsWrapper().add(stockMovementItem2);

        StockMovementItem stockMovementItem3 = new StockMovementItemBuilder()
                .withDocumentNo("3")
                .withMovementDate("2016-12-31")
                .withStockOnHand(300)
                .withQuantity(100)
                .withMovementType(MovementReasonManager.MovementType.RECEIVE)
                .build();
        stockCard.getStockMovementItemsWrapper().add(stockMovementItem3);
        viewModel = new StockHistoryViewModel(stockCard);
    }

    @Test
    public void shouldFilter() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2017-01-06", DateUtil.DB_DATE_FORMAT).getTime());
        viewModel.filter(7);
        assertEquals(1, viewModel.getFilteredMovementItemViewModelList().size());
        assertEquals("3", viewModel.getFilteredMovementItemViewModelList().get(0).getDocumentNumber());

        viewModel.filter(365);
        assertEquals(2, viewModel.getFilteredMovementItemViewModelList().size());
        assertEquals("2", viewModel.getFilteredMovementItemViewModelList().get(0).getDocumentNumber());
        assertEquals("3", viewModel.getFilteredMovementItemViewModelList().get(1).getDocumentNumber());
    }
}