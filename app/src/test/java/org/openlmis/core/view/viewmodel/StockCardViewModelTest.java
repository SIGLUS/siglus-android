package org.openlmis.core.view.viewmodel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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
        ArrayList<String> list = new ArrayList<>();
        list.add("18/10/2016");
        list.add("18/10/2017");
        list.add("18/10/2018");
        list.add("18/10/2015");
        model.setExpiryDates(list);
    }

    @Test
    public void shouldReturnFalseWhenAddDuplicateDate() throws Exception {
        boolean addExpiryDate = model.addExpiryDate("28/10/2016");
        assertTrue(addExpiryDate);
        boolean addExpiryDateSec = model.addExpiryDate("28/10/2016");
        assertFalse(addExpiryDateSec);
    }

    @Test
    public void shouldFormatExpiryDateAndSort() throws Exception {
        model.formatExpiryDateString();
        assertThat(model.getExpiryDates().get(0), is("18/10/2015"));
    }


    @Test
    public void shouldParseDraftInventory() throws Exception {
        model.setQuantity("10");
        DraftInventory draftInventory = model.parseDraftInventory();

        assertThat(draftInventory.getQuantity(), is(10L));
        assertThat(draftInventory.getExpireDates(), is("18/10/2015,18/10/2016,18/10/2017,18/10/2018"));
    }
}