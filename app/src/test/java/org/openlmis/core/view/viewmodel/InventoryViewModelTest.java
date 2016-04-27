package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class InventoryViewModelTest {

    @Test
    public void shouldBuildEmergencyModel() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setId(1);
        stockCard.setProduct(ProductBuilder.buildAdultProduct());

        InventoryViewModel inventoryViewModel = InventoryViewModel.buildEmergencyModel(stockCard);

        assertThat(inventoryViewModel.getStockCard().getId(),is(1L));
    }

    @Test
    public void shouldReturnTrueWhenProductIsArchivedAndNotQuantityIsEmpty() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setId(1);

        Product product = ProductBuilder.buildAdultProduct();
        product.setArchived(true);
        stockCard.setProduct(product);

        InventoryViewModel inventoryViewModel = InventoryViewModel.buildEmergencyModel(stockCard);
        inventoryViewModel.setChecked(true);

        assertTrue(inventoryViewModel.validate());
    }
}