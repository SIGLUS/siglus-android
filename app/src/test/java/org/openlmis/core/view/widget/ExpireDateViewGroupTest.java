package org.openlmis.core.view.widget;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import static junit.framework.Assert.assertEquals;

@RunWith(LMISTestRunner.class)
public class ExpireDateViewGroupTest extends LMISRepositoryUnitTest {

    private ExpireDateViewGroup expireDateViewGroup;

    @Before
    public void setUp() {
        expireDateViewGroup = new ExpireDateViewGroup(LMISApp.getContext());
    }

    @Test
    public void shouldInitExpireDateViewGroup () throws LMISException {
        StockCard stockCard = new StockCard();
        final Product product = new Product();
        product.setCode("08S42");
        product.setPrimaryName("drug1");
        stockCard.setProduct(product);

        stockCard.setId(1L);
        stockCard.setStockOnHand(1L);
        stockCard.setExpireDates("10/10/2016, 12/12/2016, 3/8/2017");

        final StockCardViewModel model = new StockCardViewModel(stockCard);

        expireDateViewGroup.initExpireDateViewGroup(model, true);
        assertEquals(expireDateViewGroup.getChildCount(),4);
    }
}
