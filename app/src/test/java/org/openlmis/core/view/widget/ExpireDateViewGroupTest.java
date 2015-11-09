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
    protected StockCardViewModel model;

    @Before
    public void setUp() {
        expireDateViewGroup = new ExpireDateViewGroup(LMISApp.getContext());

        StockCard stockCard = new StockCard();
        final Product product = new Product();
        product.setCode("08S42");
        product.setPrimaryName("drug1");
        stockCard.setProduct(product);

        stockCard.setId(1L);
        stockCard.setStockOnHand(1L);
        stockCard.setExpireDates("10/10/2016, 11/10/2016, 12/10/2017");

        model = new StockCardViewModel(stockCard);
    }

    @Test
    public void shouldInitExpireDateViewGroup () throws LMISException {
        expireDateViewGroup.initExpireDateViewGroup(model, true);
        assertEquals(4,expireDateViewGroup.getChildCount());
    }

    @Test
    public void shouldNotAddDateIfExisted() throws Exception {
        expireDateViewGroup.initExpireDateViewGroup(model,true);
        expireDateViewGroup.addDate("10/10/2016");
        expireDateViewGroup.addDate("13/10/2016");

        assertEquals(4,expireDateViewGroup.expireDates.size());
        assertEquals(5, expireDateViewGroup.getChildCount());
    }
}
