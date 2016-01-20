package org.openlmis.core.view.viewmodel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class StockCardViewModelTest {

    private StockCardViewModel model;
    private StockCard stockCard;

    @Before
    public void setup() {
        stockCard = new StockCard();
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
    public void shouldParseDraftInventory() throws Exception {
        model.setQuantity("10");
        DraftInventory draftInventory = model.parseDraftInventory();

        assertThat(draftInventory.getQuantity(), is(10L));
        assertThat(draftInventory.getExpireDates(), is("18/10/2015,18/10/2016,18/10/2017,18/10/2018"));
    }

    @Test
    public void shouldCheckValidationInPhysicalInventoryPage() {
        model = new StockCardViewModel(stockCard);

        // Physical Inventory should not be valid when it's empty
        model.setQuantity("");
        model.validate();
        assertFalse(model.isValid());

        //Physical Inventory should be valid when it's numerical
        model.setQuantity("100");
        model.validate();
        assertTrue(model.isValid());
    }

    @Test
    public void shouldCheckValidationInInitialInventoryPage() {
        model = new StockCardViewModel(new ProductBuilder().setCode("08S32").setPrimaryName("Primary name").build());

        // When it's not checked, it should be valid
        assertTrue(model.isValid());

        //When it's checked, but has no numerical value, should be invalid
        model.setChecked(true);
        model.validate();
        assertFalse(model.isValid());

        //When it's checked and filled with numerical value. It should be valid
        model.setQuantity("123");
        model.validate();
        assertTrue(model.isValid());
    }

    @Test
    public void shouldNotThrowExceptionIfTypeIsNull() {
        //should not throw exception
        new StockCardViewModel(new ProductBuilder().setCode("08S32").setPrimaryName("Primary name").setType(null).build()).getStyleType();
    }
}