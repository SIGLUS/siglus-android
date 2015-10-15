package org.openlmis.core.view.viewmodel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(LMISTestRunner.class)
public class StockCardViewModelTest {

    private StockCardViewModel model;

    @Before
    public void setup() {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(10);
        Product product = new Product();
        product.setPrimaryName("Product");
        stockCard.setProduct(product);
        stockCard.setExpireDates("");
        model = new StockCardViewModel(stockCard);
    }

    @Test
    public void shouldReturnTrueWhenAddExpireDate() throws Exception {
        boolean addExpiryDate = model.addExpiryDate("2015");
        assertTrue(addExpiryDate);
        boolean addExpiryDateSec = model.addExpiryDate("2015");
        assertFalse(addExpiryDateSec);
    }
}